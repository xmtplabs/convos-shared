package org.convos.bridge.codegen.swift

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import org.convos.bridge.codegen.model.BridgeModel
import org.convos.bridge.codegen.model.FunctionDescriptor
import org.convos.bridge.codegen.model.PluginDescriptor
import org.convos.bridge.codegen.model.TypeKind
import org.convos.bridge.codegen.model.TypeRef

/**
 * Emits the ConvosBridge Swift package sources: one file per plugin (protocol +
 * dispatcher), a `BridgeTypes.swift` with the shared Codable structs/enums, and
 * the verbatim runtime files from `resources/swift/`.
 *
 * Files are written through KSP with package names that map onto the SPM
 * directory layout (`swift/Sources/ConvosBridge/...`); the `:plugins` build
 * mirrors that tree to `<repoRoot>/ConvosBridge/`. The package manifest is the
 * hand-written repo-root `Package.swift`.
 */
class SwiftBridgeGenerator(
    private val codeGenerator: CodeGenerator,
) {
    fun generate(model: BridgeModel) {
        if (model.structs.isNotEmpty() || model.enums.isNotEmpty()) {
            writeSwiftFile("BridgeTypes", generateTypes(model))
        }
        for (plugin in model.plugins) {
            writeSwiftFile(plugin.interfaceName, generatePlugin(plugin))
        }
        copySwiftResource("/swift/BridgeCore.swift", "BridgeCore")
        copySwiftResource("/swift/ConvosWebBridge.swift", "ConvosWebBridge")
    }

    private fun generateTypes(model: BridgeModel): String = buildString {
        appendLine(HEADER)
        appendLine()
        for (enum in model.enums) {
            appendLine("public enum ${enum.name}: String, Codable, Sendable {")
            for (value in enum.values) {
                appendLine("    case $value")
            }
            appendLine("}")
            appendLine()
        }
        for (struct in model.structs) {
            appendLine("public struct ${struct.name}: Codable, Sendable {")
            for (property in struct.properties) {
                appendLine("    public let ${property.name}: ${swiftType(property.type)}")
            }
            appendLine()
            val parameters = struct.properties.joinToString(", ") { "${it.name}: ${swiftType(it.type)}" }
            appendLine("    public init($parameters) {")
            for (property in struct.properties) {
                appendLine("        self.${property.name} = ${property.name}")
            }
            appendLine("    }")
            appendLine("}")
            appendLine()
        }
    }

    private fun generatePlugin(plugin: PluginDescriptor): String = buildString {
        appendLine(HEADER)
        appendLine()
        appendLine("public protocol ${plugin.interfaceName}: AnyObject {")
        for (function in plugin.functions) {
            appendLine("    func ${signature(function)}")
        }
        appendLine("}")
        appendLine()
        appendLine("public final class ${plugin.interfaceName}Dispatcher: BridgePluginDispatcher {")
        appendLine("    public let pluginName = \"${plugin.pluginName}\"")
        appendLine("    private let impl: ${plugin.interfaceName}")
        appendLine()
        appendLine("    public init(_ impl: ${plugin.interfaceName}) {")
        appendLine("        self.impl = impl")
        appendLine("    }")
        appendLine()
        appendLine("    public func handle(method: String, params: BridgeParams) async throws -> Any? {")
        appendLine("        switch method {")
        for (function in plugin.functions.filter { !it.isEvent }) {
            appendDispatchCase(function)
        }
        appendLine("        default:")
        appendLine("            throw BridgeError.methodNotFound(plugin: pluginName, method: method)")
        appendLine("        }")
        appendLine("    }")
        val events = plugin.functions.filter { it.isEvent }
        if (events.isNotEmpty()) {
            appendLine()
            appendEventsMethod(events)
        }
        appendLine("}")
    }

    /**
     * Bridges each `AsyncStream<T>` event to an `AsyncStream<Any>` of
     * JSON-encodable payloads that `BridgeDispatcher` forwards to the page.
     */
    private fun StringBuilder.appendEventsMethod(events: List<FunctionDescriptor>) {
        appendLine("    public func events(_ name: String) -> AsyncStream<Any>? {")
        appendLine("        switch name {")
        for (function in events) {
            appendLine("        case \"${function.name}\":")
            appendLine("            return AsyncStream { continuation in")
            appendLine("                let task = Task {")
            appendLine("                    for await value in impl.${function.name}() {")
            appendLine("                        if let encoded = try? BridgeJSON.encodeResult(value) {")
            appendLine("                            continuation.yield(encoded)")
            appendLine("                        }")
            appendLine("                    }")
            appendLine("                    continuation.finish()")
            appendLine("                }")
            appendLine("                continuation.onTermination = { _ in task.cancel() }")
            appendLine("            }")
        }
        appendLine("        default:")
        appendLine("            return nil")
        appendLine("        }")
        appendLine("    }")
    }

    private fun signature(function: FunctionDescriptor): String {
        val parameters = function.parameters.joinToString(", ") { "${it.name}: ${swiftType(it.type)}" }
        val effects = if (function.isSuspend) " async throws" else ""
        val returns = if (function.returnType.kind == TypeKind.UNIT) "" else " -> ${swiftType(function.returnType)}"
        return "${function.name}($parameters)$effects$returns"
    }

    private fun StringBuilder.appendDispatchCase(function: FunctionDescriptor) {
        val arguments = function.parameters.joinToString(", ") { param ->
            "${param.name}: try params.decode(\"${param.name}\")"
        }
        val await = if (function.isSuspend) "try await " else ""
        val call = "${await}impl.${function.name}($arguments)"
        appendLine("        case \"${function.name}\":")
        if (function.returnType.kind == TypeKind.UNIT) {
            appendLine("            $call")
            appendLine("            return nil")
        } else {
            appendLine("            return try BridgeJSON.encodeResult($call)")
        }
    }

    private fun swiftType(type: TypeRef): String {
        val base = when (type.kind) {
            TypeKind.STRING -> "String"
            TypeKind.INT -> "Int"
            TypeKind.LONG -> "Int64"
            TypeKind.BOOLEAN -> "Bool"
            TypeKind.DOUBLE -> "Double"
            TypeKind.FLOAT -> "Float"
            TypeKind.UNIT -> "Void"
            TypeKind.ENUM, TypeKind.STRUCT -> type.name
            TypeKind.LIST -> "[${swiftType(type.element!!)}]"
            TypeKind.FLOW -> "AsyncStream<${swiftType(type.element!!)}>"
        }
        return if (type.nullable) "$base?" else base
    }

    private fun writeSwiftFile(fileName: String, content: String) {
        val normalized = content.trimEnd('\n') + "\n"
        codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true),
            packageName = SOURCES_PACKAGE,
            fileName = fileName,
            extensionName = "swift",
        ).bufferedWriter().use { it.write(normalized) }
    }

    private fun copySwiftResource(resourcePath: String, fileName: String) {
        val content = javaClass.getResourceAsStream(resourcePath)?.bufferedReader()?.use { it.readText() }
            ?: error("Missing Swift resource: $resourcePath")
        writeSwiftFile(fileName, content)
    }

    companion object {
        private const val LIBRARY_NAME = "ConvosBridge"
        private const val SOURCES_PACKAGE = "swift.Sources.$LIBRARY_NAME"
        private const val HEADER = "// AUTOGENERATED by the convos bridge codegen processor. Do not edit."
    }
}
