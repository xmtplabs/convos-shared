package org.convos.metrics.codegen.kotlin

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import org.convos.metrics.codegen.core.CoreActionsDescriptor
import org.convos.metrics.codegen.core.CoreMetricsModel
import org.convos.metrics.codegen.navigation.NavigationGraph
import org.convos.metrics.codegen.navigation.NavigationMethodType
import org.convos.metrics.codegen.navigation.NavigationTargetDescriptor

class KotlinGenerator(
    private val codeGenerator: CodeGenerator,
) {
    fun generateCollectors(graph: NavigationGraph) {
        for (descriptor in graph.targets.values) {
            generateCollector(descriptor)
        }
    }

    fun generateCore(model: CoreMetricsModel) {
        val packageName = corePackageName(model) ?: return
        model.coreActions?.let { generateMetricsCoreActions(packageName, it, model) }
        generateCoreMetrics(packageName, model)
    }

    private fun corePackageName(model: CoreMetricsModel): String? {
        val qualified = model.userProperties?.qualifiedName
            ?: model.coreActions?.qualifiedName
            ?: return null
        return qualified.substringBeforeLast('.')
    }

    private fun generateMetricsCoreActions(
        packageName: String,
        descriptor: CoreActionsDescriptor,
        model: CoreMetricsModel,
    ) {
        val referencedEnums = model.enumTypes.filter { enumType ->
            descriptor.actions.any { it.parameters.any { p -> p.isEnum && p.type == enumType.name } }
        }

        val code = buildString {
            appendLine("package $packageName")
            appendLine()
            appendLine("import $COLLECTOR_DELEGATE_FQN")
            val crossPackageEnums = referencedEnums.filter { enumType ->
                enumType.qualifiedName.isNotEmpty() &&
                    enumType.qualifiedName != enumType.name &&
                    enumType.qualifiedName.substringBeforeLast('.') != packageName
            }
            for (enumType in crossPackageEnums.sortedBy { it.qualifiedName }) {
                appendLine("import ${enumType.qualifiedName}")
            }
            appendLine()
            for (enumType in referencedEnums) {
                appendLine("private fun ${enumType.name}.metricsString(): String = when (this) {")
                for (value in enumType.values) {
                    appendLine("    ${enumType.name}.${value.name} -> \"${value.snakeName}\"")
                }
                appendLine("}")
                appendLine()
            }
            appendLine("class MetricsCoreActions(")
            appendLine("    private val delegate: CollectorDelegate,")
            appendLine(") : ${descriptor.name} {")

            for (action in descriptor.actions) {
                val params = action.parameters.joinToString(", ") { p ->
                    val baseType = if (p.qualifiedType == "kotlin.collections.List" && p.elementType != null) {
                        "List<${p.elementType}>"
                    } else {
                        p.type
                    }
                    "${p.name}: $baseType${if (p.nullable) "?" else ""}"
                }
                val suspendKw = if (action.isSuspend) "suspend " else ""
                appendLine()
                appendLine("    override ${suspendKw}fun ${action.name}($params) {")
                appendLine("        delegate.sendEvent(")
                appendLine("            ${eventConstantName(action.eventName)},")
                if (action.parameters.isEmpty()) {
                    appendLine("            emptyMap(),")
                } else {
                    appendLine("            mapOf(")
                    for (p in action.parameters) {
                        val valueExpr = if (p.isEnum) {
                            if (p.nullable) "${p.name}?.metricsString()" else "${p.name}.metricsString()"
                        } else {
                            p.name
                        }
                        appendLine("                ${paramConstantName(p.snakeName)} to $valueExpr,")
                    }
                    appendLine("            ),")
                }
                appendLine("        )")
                appendLine("    }")
            }

            appendLine()
            appendLine("    companion object {")
            for (action in descriptor.actions) {
                appendLine("        const val ${eventConstantName(action.eventName)} = \"${action.eventName}\"")
            }
            val uniqueParams = descriptor.actions
                .flatMap { it.parameters }
                .distinctBy { it.snakeName }
            for (p in uniqueParams) {
                appendLine("        const val ${paramConstantName(p.snakeName)} = \"${p.snakeName}\"")
            }
            appendLine("    }")
            appendLine("}")
        }

        codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true),
            packageName = packageName,
            fileName = "MetricsCoreActions",
        ).bufferedWriter().use { it.write(code) }
    }

    private fun generateCoreMetrics(packageName: String, model: CoreMetricsModel) {
        val userProps = model.userProperties
        val coreActions = model.coreActions

        val code = buildString {
            appendLine("package $packageName")
            appendLine()
            appendLine("import $COLLECTOR_DELEGATE_FQN")
            appendLine("import $METRICS_STABLE_ID_FQN")
            appendLine()
            appendLine("class CoreMetrics(")
            appendLine("    private val delegate: CollectorDelegate,")
            appendLine("    private val stableId: MetricsStableIdEncoder,")
            appendLine(") {")

            if (coreActions != null) {
                appendLine("    val actions: ${coreActions.name} = MetricsCoreActions(delegate)")
                appendLine()
            }

            appendLine("    fun identify(privateKey: ByteArray) {")
            appendLine("        delegate.identify(stableId.derive(privateKey))")
            appendLine("    }")

            if (userProps != null) {
                appendLine()
                appendLine("    suspend fun updateUserProperties(properties: ${userProps.name}) {")
                if (userProps.properties.isEmpty()) {
                    appendLine("        delegate.updateUserProperties(emptyMap())")
                } else {
                    appendLine("        delegate.updateUserProperties(")
                    appendLine("            mapOf(")
                    for (p in userProps.properties) {
                        appendLine("                ${userPropertyConstantName(p.snakeName)} to properties.${p.name},")
                    }
                    appendLine("            ),")
                    appendLine("        )")
                }
                appendLine("    }")
            }

            if (userProps != null && userProps.properties.isNotEmpty()) {
                appendLine()
                appendLine("    companion object {")
                for (p in userProps.properties) {
                    appendLine("        const val ${userPropertyConstantName(p.snakeName)} = \"${p.snakeName}\"")
                }
                appendLine("    }")
            }

            appendLine("}")
        }

        codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true),
            packageName = packageName,
            fileName = "CoreMetrics",
        ).bufferedWriter().use { it.write(code) }
    }

    private fun eventConstantName(snakeEvent: String): String =
        "EVENT_${snakeEvent.uppercase()}"

    private fun paramConstantName(snakeParam: String): String =
        "PARAM_${snakeParam.uppercase()}"

    private fun userPropertyConstantName(snakeProperty: String): String =
        "USER_PROPERTY_${snakeProperty.uppercase()}"

    companion object {
        private const val COLLECTOR_DELEGATE_FQN =
            "org.convos.metrics.descriptors.navigation.CollectorDelegate"
        private const val METRICS_STABLE_ID_FQN =
            "org.convos.metrics.descriptors.MetricsStableIdEncoder"
    }

    private fun generateCollector(descriptor: NavigationTargetDescriptor) {
        val packageName = descriptor.qualifiedName.substringBeforeLast('.')
        val className = "${descriptor.name.removeSuffix("Navigator")}Collector"

        val code = buildString {
            appendLine("package $packageName")
            appendLine()
            appendLine("class $className(")
            appendLine("    private val instance: ${descriptor.name},")
            appendLine("    private val delegate: CollectorDelegate,")
            appendLine(") : ${descriptor.name} {")

            for (method in descriptor.navigationMethods) {
                val methodName = when (method.methodType) {
                    NavigationMethodType.NAVIGATE_TO -> "navigateTo"
                    NavigationMethodType.PRESENT -> "present"
                    else -> continue
                }
                val delegateMethod = when (method.methodType) {
                    NavigationMethodType.NAVIGATE_TO -> "navigatedTo"
                    NavigationMethodType.PRESENT -> "presented"
                    else -> continue
                }
                val targetCollector = "${method.targetScreen.removeSuffix("Navigator")}Collector"
                appendLine()
                appendLine("    override fun $methodName(${method.parameterName}: ${method.targetScreen}.Args) {")
                appendLine("        delegate.$delegateMethod(")
                appendLine("            source = NAME,")
                appendLine("            target = $targetCollector.NAME,")
                appendLine("        )")
                appendLine("        instance.$methodName(${method.parameterName})")
                appendLine("    }")
            }

            if (descriptor.hasClosed) {
                appendLine()
                appendLine("    override fun closed(context: ScreenContext) {")
                appendLine("        delegate.closed(screen = NAME, context = context)")
                appendLine("        instance.closed(context)")
                appendLine("    }")
            }

            appendLine()
            appendLine("    companion object {")
            appendLine("        const val NAME = \"${descriptor.metricsName}\"")
            appendLine("    }")

            appendLine("}")
            appendLine()
        }

        codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true),
            packageName = packageName,
            fileName = className,
        ).bufferedWriter().use { it.write(code) }
    }
}
