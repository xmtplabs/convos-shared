package org.convos.metrics.codegen.swift

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import org.convos.metrics.codegen.core.CoreActionsDescriptor
import org.convos.metrics.codegen.core.CoreEnumType
import org.convos.metrics.codegen.core.CoreMetricsModel
import org.convos.metrics.codegen.core.UserPropertiesDescriptor
import org.convos.metrics.codegen.navigation.NavigationGraph
import org.convos.metrics.codegen.navigation.NavigationMethodType
import org.convos.metrics.codegen.navigation.NavigationTargetDescriptor

class SwiftGenerator(
    private val codeGenerator: CodeGenerator,
) {
    fun generate(graph: NavigationGraph, coreModel: CoreMetricsModel) {
        generatePackageManifest()
        generateCollectorDelegate()
        copySwiftResource("/swift/MetricsStableIdEncoder.swift", "MetricsStableIdEncoder")
        if (graph.targets.isNotEmpty()) {
            generateNavigationTargets(graph)
            generateCollectors(graph)
        }
        if (!coreModel.isEmpty) {
            generateCore(coreModel)
        }
    }

    private fun generatePackageManifest() {
        val code = buildString {
            appendLine("// swift-tools-version: 5.9")
            appendLine()
            appendLine("import PackageDescription")
            appendLine()
            appendLine("let package = Package(")
            appendLine("    name: \"$LIBRARY_NAME\",")
            appendLine("    platforms: [")
            appendLine("        .iOS(.v13),")
            appendLine("        .macOS(.v10_15),")
            appendLine("    ],")
            appendLine("    products: [")
            appendLine("        .library(name: \"$LIBRARY_NAME\", targets: [\"$LIBRARY_NAME\"]),")
            appendLine("    ],")
            appendLine("    targets: [")
            appendLine("        .target(name: \"$LIBRARY_NAME\"),")
            appendLine("    ]")
            appendLine(")")
        }

        writeSwiftFile("swift", "Package", code)
    }

    private fun generateCollectorDelegate() {
        val code = buildString {
            appendLine("public struct ScreenContext {")
            appendLine("    public let durationSecs: Float")
            appendLine()
            appendLine("    public init(durationSecs: Float) {")
            appendLine("        self.durationSecs = durationSecs")
            appendLine("    }")
            appendLine("}")
            appendLine()
            appendLine("open class CollectorDelegate {")
            appendLine("    public init() {}")
            appendLine()
            appendLine("    open func navigatedTo(source: String, target: String) {}")
            appendLine("    open func presented(source: String, target: String) {}")
            appendLine("    open func closed(screen: String, context: ScreenContext) {}")
            appendLine()
            appendLine("    open func identify(userId: String) {}")
            appendLine("    open func updateUserProperties(properties: [String: Any?]) {}")
            appendLine("    open func sendEvent(name: String, properties: [String: Any?]) {}")
            appendLine("}")
            appendLine()
        }

        writeSwiftFile(SOURCES_PACKAGE, "CollectorDelegate", code)
    }

    private fun generateNavigationTargets(graph: NavigationGraph) {
        val code = buildString {
            for (enumType in graph.enumTypes) {
                appendLine("public enum ${enumType.name} {")
                for (value in enumType.values) {
                    appendLine("    case ${value.toSwiftEnumCase()}")
                }
                appendLine("}")
                appendLine()
            }

            for (descriptor in graph.targets.values) {
                generateArgsStruct(this, descriptor)
                appendLine()
            }

            for (descriptor in graph.targets.values) {
                generateProtocol(this, descriptor)
                appendLine()
            }
        }

        writeSwiftFile(SOURCES_PACKAGE, "NavigationTargets", code)
    }

    private fun generateArgsStruct(sb: StringBuilder, descriptor: NavigationTargetDescriptor) {
        val structName = "${descriptor.name}Args"
        val params = descriptor.args.parameters

        sb.appendLine("public struct $structName {")

        if (params.isEmpty()) {
            sb.appendLine("    public init() {}")
        } else {
            for (param in params) {
                val swiftType = kotlinTypeToSwift(param.type, param.qualifiedType, null, null)
                val typeStr = if (param.nullable) "$swiftType?" else swiftType
                sb.appendLine("    public let ${param.name}: $typeStr")
            }
            sb.appendLine()

            val initParams = params.joinToString(", ") { param ->
                val swiftType = kotlinTypeToSwift(param.type, param.qualifiedType, null, null)
                val typeStr = if (param.nullable) "$swiftType?" else swiftType
                val default = if (param.hasDefault && param.nullable) " = nil" else ""
                "${param.name}: $typeStr$default"
            }
            sb.appendLine("    public init($initParams) {")
            for (param in params) {
                sb.appendLine("        self.${param.name} = ${param.name}")
            }
            sb.appendLine("    }")
        }

        sb.appendLine("}")
    }

    private fun generateProtocol(sb: StringBuilder, descriptor: NavigationTargetDescriptor) {
        sb.appendLine("public protocol ${descriptor.name}: AnyObject {")

        for (method in descriptor.navigationMethods) {
            val methodName = when (method.methodType) {
                NavigationMethodType.NAVIGATE_TO -> "navigateTo"
                NavigationMethodType.PRESENT -> "present"
                else -> continue
            }
            sb.appendLine("    func $methodName(${method.parameterName}: ${method.targetScreen}Args)")
        }

        if (descriptor.hasClosed) {
            sb.appendLine("    func closed(context: ScreenContext)")
        }

        sb.appendLine("}")
    }

    private fun generateCollectors(graph: NavigationGraph) {
        val code = buildString {
            for ((index, descriptor) in graph.targets.values.withIndex()) {
                if (index > 0) appendLine()
                generateCollector(this, descriptor)
            }
            appendLine()
        }

        writeSwiftFile(SOURCES_PACKAGE, "NavigationCollectors", code)
    }

    private fun generateCollector(sb: StringBuilder, descriptor: NavigationTargetDescriptor) {
        val className = "${descriptor.name.removeSuffix("Navigator")}Collector"

        sb.appendLine("public class $className: ${descriptor.name} {")
        sb.appendLine("    private weak var instance: ${descriptor.name}?")
        sb.appendLine("    private weak var delegate: CollectorDelegate?")
        sb.appendLine()
        sb.appendLine("    public init(instance: ${descriptor.name}, delegate: CollectorDelegate) {")
        sb.appendLine("        self.instance = instance")
        sb.appendLine("        self.delegate = delegate")
        sb.appendLine("    }")

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
            sb.appendLine()
            sb.appendLine("    public func $methodName(${method.parameterName}: ${method.targetScreen}Args) {")
            sb.appendLine("        delegate?.$delegateMethod(source: Self.name, target: $targetCollector.name)")
            sb.appendLine("        instance?.$methodName(${method.parameterName}: ${method.parameterName})")
            sb.appendLine("    }")
        }

        if (descriptor.hasClosed) {
            sb.appendLine()
            sb.appendLine("    public func closed(context: ScreenContext) {")
            sb.appendLine("        delegate?.closed(screen: Self.name, context: context)")
            sb.appendLine("        instance?.closed(context: context)")
            sb.appendLine("    }")
        }

        sb.appendLine()
        sb.appendLine("    public static let name: String = \"${descriptor.metricsName}\"")
        sb.appendLine("}")
    }

    private fun generateCore(model: CoreMetricsModel) {
        if (model.enumTypes.isNotEmpty()) {
            generateCoreEnums(model.enumTypes)
        }
        model.userProperties?.let { generateUserPropertiesStruct(it) }
        model.coreActions?.let {
            generateCoreActionsProtocol(it)
            generateMetricsCoreActions(it)
        }
        generateCoreMetrics(model)
    }

    private fun generateCoreEnums(enumTypes: List<CoreEnumType>) {
        val code = buildString {
            for ((index, enumType) in enumTypes.withIndex()) {
                if (index > 0) appendLine()
                appendLine("public enum ${enumType.name} {")
                for (value in enumType.values) {
                    appendLine("    case ${value.name.toSwiftEnumCase()}")
                }
                appendLine("}")
                appendLine()
                appendLine("extension ${enumType.name} {")
                appendLine("    public var metricsString: String {")
                appendLine("        switch self {")
                for (value in enumType.values) {
                    appendLine("        case .${value.name.toSwiftEnumCase()}: return \"${value.snakeName}\"")
                }
                appendLine("        }")
                appendLine("    }")
                appendLine("}")
            }
        }

        writeSwiftFile(SOURCES_PACKAGE, "CoreEnums", code)
    }

    private fun generateUserPropertiesStruct(descriptor: UserPropertiesDescriptor) {
        val code = buildString {
            appendLine("public struct ${descriptor.name}: Sendable {")
            for (p in descriptor.properties) {
                val swiftType = kotlinTypeToSwift(p.type, p.qualifiedType, p.elementType, p.elementQualifiedType)
                val typeStr = if (p.nullable) "$swiftType?" else swiftType
                appendLine("    public let ${p.name}: $typeStr")
            }
            if (descriptor.properties.isNotEmpty()) {
                appendLine()
                appendLine("    public init(")
                val lastIndex = descriptor.properties.size - 1
                for ((index, p) in descriptor.properties.withIndex()) {
                    val swiftType = kotlinTypeToSwift(p.type, p.qualifiedType, p.elementType, p.elementQualifiedType)
                    val typeStr = if (p.nullable) "$swiftType?" else swiftType
                    val default = if (p.nullable) " = nil" else ""
                    val comma = if (index < lastIndex) "," else ""
                    appendLine("        ${p.name}: $typeStr$default$comma")
                }
                appendLine("    ) {")
                for (p in descriptor.properties) {
                    appendLine("        self.${p.name} = ${p.name}")
                }
                appendLine("    }")
            } else {
                appendLine("    public init() {}")
            }
            appendLine("}")
        }

        writeSwiftFile(SOURCES_PACKAGE, descriptor.name, code)
    }

    private fun generateCoreActionsProtocol(descriptor: CoreActionsDescriptor) {
        val code = buildString {
            appendLine("public protocol ${descriptor.name}: AnyObject, Sendable {")
            for (action in descriptor.actions) {
                val params = action.parameters.joinToString(", ") { p ->
                    val swiftType = kotlinTypeToSwift(p.type, p.qualifiedType, p.elementType, p.elementQualifiedType)
                    val typeStr = if (p.nullable) "$swiftType?" else swiftType
                    "${p.name}: $typeStr"
                }
                val asyncKw = if (action.isSuspend) " async" else ""
                appendLine("    func ${action.name}($params)$asyncKw")
            }
            appendLine("}")
        }

        writeSwiftFile(SOURCES_PACKAGE, descriptor.name, code)
    }

    private fun generateMetricsCoreActions(descriptor: CoreActionsDescriptor) {
        val code = buildString {
            appendLine("public final class MetricsCoreActions: ${descriptor.name}, @unchecked Sendable {")
            appendLine("    private weak var delegate: CollectorDelegate?")
            appendLine()
            appendLine("    public init(delegate: CollectorDelegate) {")
            appendLine("        self.delegate = delegate")
            appendLine("    }")

            for (action in descriptor.actions) {
                val params = action.parameters.joinToString(", ") { p ->
                    val swiftType = kotlinTypeToSwift(p.type, p.qualifiedType, p.elementType, p.elementQualifiedType)
                    val typeStr = if (p.nullable) "$swiftType?" else swiftType
                    "${p.name}: $typeStr"
                }
                val asyncKw = if (action.isSuspend) " async" else ""
                appendLine()
                appendLine("    public func ${action.name}($params)$asyncKw {")
                if (action.parameters.isEmpty()) {
                    appendLine("        delegate?.sendEvent(name: Self.${eventConstantName(action.eventName)}, properties: [:])")
                } else {
                    appendLine("        delegate?.sendEvent(name: Self.${eventConstantName(action.eventName)}, properties: [")
                    for (p in action.parameters) {
                        val valueExpr = if (p.isEnum) {
                            if (p.nullable) "${p.name}?.metricsString" else "${p.name}.metricsString"
                        } else {
                            p.name
                        }
                        appendLine("            Self.${paramConstantName(p.snakeName)}: $valueExpr,")
                    }
                    appendLine("        ])")
                }
                appendLine("    }")
            }

            appendLine()
            for (action in descriptor.actions) {
                appendLine("    public static let ${eventConstantName(action.eventName)}: String = \"${action.eventName}\"")
            }
            val uniqueParams = descriptor.actions
                .flatMap { it.parameters }
                .distinctBy { it.snakeName }
            for (p in uniqueParams) {
                appendLine("    public static let ${paramConstantName(p.snakeName)}: String = \"${p.snakeName}\"")
            }
            appendLine("}")
        }

        writeSwiftFile(SOURCES_PACKAGE, "MetricsCoreActions", code)
    }

    private fun generateCoreMetrics(model: CoreMetricsModel) {
        val userProps = model.userProperties
        val coreActions = model.coreActions

        val code = buildString {
            appendLine("import Foundation")
            appendLine()
            appendLine("public final class CoreMetrics: @unchecked Sendable {")
            if (coreActions != null) {
                appendLine("    public let actions: ${coreActions.name}")
            }
            appendLine("    private weak var delegate: CollectorDelegate?")
            appendLine("    private let stableId: MetricsStableIdEncoder")
            appendLine()
            appendLine("    public init(delegate: CollectorDelegate, stableId: MetricsStableIdEncoder) {")
            appendLine("        self.delegate = delegate")
            appendLine("        self.stableId = stableId")
            if (coreActions != null) {
                appendLine("        self.actions = MetricsCoreActions(delegate: delegate)")
            }
            appendLine("    }")
            appendLine()
            appendLine("    public func identify(privateKey: Data) {")
            appendLine("        delegate?.identify(userId: stableId.derive(privateKey: privateKey))")
            appendLine("    }")

            if (userProps != null) {
                appendLine()
                appendLine("    public func updateUserProperties(properties: ${userProps.name}) async {")
                if (userProps.properties.isEmpty()) {
                    appendLine("        delegate?.updateUserProperties(properties: [:])")
                } else {
                    appendLine("        delegate?.updateUserProperties(properties: [")
                    for (p in userProps.properties) {
                        appendLine("            Self.${userPropertyConstantName(p.snakeName)}: properties.${p.name},")
                    }
                    appendLine("        ])")
                }
                appendLine("    }")
            }

            if (userProps != null && userProps.properties.isNotEmpty()) {
                appendLine()
                for (p in userProps.properties) {
                    appendLine("    public static let ${userPropertyConstantName(p.snakeName)}: String = \"${p.snakeName}\"")
                }
            }

            appendLine("}")
        }

        writeSwiftFile(SOURCES_PACKAGE, "CoreMetrics", code)
    }

    private fun writeSwiftFile(packageName: String, fileName: String, content: String) {
        val normalized = content.trimEnd('\n') + "\n"
        codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true),
            packageName = packageName,
            fileName = fileName,
            extensionName = "swift",
        ).bufferedWriter().use { it.write(normalized) }
    }

    private fun copySwiftResource(resourcePath: String, fileName: String) {
        val content = javaClass.getResourceAsStream(resourcePath)?.bufferedReader()?.use { it.readText() }
            ?: error("Missing Swift resource: $resourcePath")
        writeSwiftFile(SOURCES_PACKAGE, fileName, content)
    }

    companion object {
        private const val LIBRARY_NAME = "ConvosMetrics"
        private const val SOURCES_PACKAGE = "swift.Sources.$LIBRARY_NAME"

        private fun eventConstantName(snakeEvent: String): String =
            "event${snakeEvent.snakeToUpperCamel()}"

        private fun paramConstantName(snakeParam: String): String =
            "param${snakeParam.snakeToUpperCamel()}"

        private fun userPropertyConstantName(snakeProperty: String): String =
            "userProperty${snakeProperty.snakeToUpperCamel()}"

        private fun String.snakeToUpperCamel(): String =
            split("_")
                .filter { it.isNotEmpty() }
                .joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }

        private fun kotlinTypeToSwift(
            type: String,
            qualifiedType: String,
            elementType: String?,
            elementQualifiedType: String?,
        ): String {
            return when (qualifiedType) {
                "kotlin.String" -> "String"
                "kotlin.Int" -> "Int"
                "kotlin.Long" -> "Int64"
                "kotlin.Boolean" -> "Bool"
                "kotlin.Double" -> "Double"
                "kotlin.Float" -> "Float"
                "kotlin.collections.List" -> {
                    val element = if (elementType != null && elementQualifiedType != null) {
                        kotlinTypeToSwift(elementType, elementQualifiedType, null, null)
                    } else {
                        "Any"
                    }
                    "[$element]"
                }
                else -> type
            }
        }

        private fun String.toSwiftEnumCase(): String =
            lowercase()
                .split("_")
                .mapIndexed { i, part ->
                    if (i == 0) part else part.replaceFirstChar { it.uppercase() }
                }
                .joinToString("")
    }
}
