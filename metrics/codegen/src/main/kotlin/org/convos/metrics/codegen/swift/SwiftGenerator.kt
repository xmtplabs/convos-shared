package org.convos.metrics.codegen.swift

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import org.convos.metrics.codegen.navigation.NavigationGraph
import org.convos.metrics.codegen.navigation.NavigationMethodType
import org.convos.metrics.codegen.navigation.NavigationTargetDescriptor

class SwiftGenerator(
    private val codeGenerator: CodeGenerator,
) {
    fun generate(graph: NavigationGraph) {
        generatePackageManifest()
        generateCollectorDelegate()
        generateNavigationTargets(graph)
        generateCollectors(graph)
    }

    private fun generatePackageManifest() {
        val code = buildString {
            appendLine("// swift-tools-version: 5.9")
            appendLine()
            appendLine("import PackageDescription")
            appendLine()
            appendLine("let package = Package(")
            appendLine("    name: \"NavigationMetrics\",")
            appendLine("    products: [")
            appendLine("        .library(name: \"NavigationMetrics\", targets: [\"NavigationMetrics\"]),")
            appendLine("    ],")
            appendLine("    targets: [")
            appendLine("        .target(name: \"NavigationMetrics\"),")
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
            appendLine("}")
            appendLine()
        }

        writeSwiftFile(SOURCES_PACKAGE, "CollectorDelegate", code)
    }

    private fun generateNavigationTargets(graph: NavigationGraph) {
        val code = buildString {
            // Enums
            for (enumType in graph.enumTypes) {
                appendLine("public enum ${enumType.name} {")
                for (value in enumType.values) {
                    appendLine("    case ${value.toSwiftEnumCase()}")
                }
                appendLine("}")
                appendLine()
            }

            // Args structs
            for (descriptor in graph.targets.values) {
                generateArgsStruct(this, descriptor)
                appendLine()
            }

            // Protocols
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
                val swiftType = kotlinTypeToSwift(param.type, param.qualifiedType)
                val typeStr = if (param.nullable) "$swiftType?" else swiftType
                sb.appendLine("    public let ${param.name}: $typeStr")
            }
            sb.appendLine()

            val initParams = params.joinToString(", ") { param ->
                val swiftType = kotlinTypeToSwift(param.type, param.qualifiedType)
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
        sb.appendLine("    public static let name = \"${descriptor.metricsName}\"")
        sb.appendLine("}")
    }

    private fun writeSwiftFile(packageName: String, fileName: String, content: String) {
        codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true),
            packageName = packageName,
            fileName = fileName,
            extensionName = "swift",
        ).bufferedWriter().use { it.write(content) }
    }

    companion object {
        private const val SOURCES_PACKAGE = "swift.Sources.NavigationMetrics"

        private fun kotlinTypeToSwift(type: String, qualifiedType: String): String {
            return when (qualifiedType) {
                "kotlin.String" -> "String"
                "kotlin.Int" -> "Int"
                "kotlin.Long" -> "Int64"
                "kotlin.Boolean" -> "Bool"
                "kotlin.Double" -> "Double"
                "kotlin.Float" -> "Float"
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
