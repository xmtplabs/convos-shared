package org.convos.metrics.codegen.kotlin

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
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
