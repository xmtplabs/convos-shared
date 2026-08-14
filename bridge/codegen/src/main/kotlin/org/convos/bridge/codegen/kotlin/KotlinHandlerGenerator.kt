package org.convos.bridge.codegen.kotlin

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import org.convos.bridge.codegen.model.BridgeModel
import org.convos.bridge.codegen.model.FunctionDescriptor
import org.convos.bridge.codegen.model.PluginDescriptor
import org.convos.bridge.codegen.model.TypeKind

/**
 * Emits one `<Interface>Handler` per plugin plus an aggregate `bridgeHandlers`
 * factory, all into [GENERATED_PACKAGE]. The handlers adapt the JSON envelope
 * world of `org.convos.bridge.runtime` to the typed plugin interfaces.
 */
class KotlinHandlerGenerator(
    private val codeGenerator: CodeGenerator,
) {
    fun generate(model: BridgeModel) {
        for (plugin in model.plugins) {
            writeKotlinFile("${plugin.interfaceName}Handler", generateHandler(plugin))
        }
        writeKotlinFile("BridgeHandlers", generateFactory(model.plugins))
    }

    private fun generateHandler(plugin: PluginDescriptor): String = buildString {
        val events = plugin.functions.filter { it.isEvent }
        val calls = plugin.functions.filter { !it.isEvent }

        appendLine(HEADER)
        appendLine("package $GENERATED_PACKAGE")
        appendLine()
        if (events.isNotEmpty()) {
            appendLine("import kotlinx.coroutines.flow.Flow")
            appendLine("import kotlinx.coroutines.flow.map")
        }
        appendLine("import kotlinx.serialization.json.JsonElement")
        appendLine("import kotlinx.serialization.json.JsonNull")
        appendLine("import kotlinx.serialization.json.JsonObject")
        appendLine("import kotlinx.serialization.json.encodeToJsonElement")
        appendLine("import ${plugin.qualifiedName}")
        appendLine("import org.convos.bridge.runtime.BridgeMethodNotFoundException")
        appendLine("import org.convos.bridge.runtime.PluginHandler")
        appendLine("import org.convos.bridge.runtime.bridgeJson")
        appendLine("import org.convos.bridge.runtime.decodeParam")
        appendLine()
        appendLine("class ${plugin.interfaceName}Handler(")
        appendLine("    private val impl: ${plugin.interfaceName},")
        appendLine(") : PluginHandler {")
        appendLine("    override val pluginName: String = \"${plugin.pluginName}\"")
        appendLine()
        appendLine("    override suspend fun handle(method: String, params: JsonObject): JsonElement = when (method) {")
        for (function in calls) {
            appendLine("        \"${function.name}\" -> ${invocation(function)}")
        }
        appendLine("        else -> throw BridgeMethodNotFoundException(pluginName, method)")
        appendLine("    }")
        if (events.isNotEmpty()) {
            appendLine()
            appendLine("    override fun events(event: String): Flow<JsonElement>? = when (event) {")
            for (function in events) {
                appendLine(
                    "        \"${function.name}\" -> " +
                        "impl.${function.name}().map { bridgeJson.encodeToJsonElement(it) }",
                )
            }
            appendLine("        else -> null")
            appendLine("    }")
        }
        appendLine("}")
    }

    private fun invocation(function: FunctionDescriptor): String {
        val arguments = function.parameters.joinToString(", ") { param ->
            "${param.name} = params.decodeParam(\"${param.name}\")"
        }
        val call = "impl.${function.name}($arguments)"
        return if (function.returnType.kind == TypeKind.UNIT) {
            "{ $call; JsonNull }"
        } else {
            "bridgeJson.encodeToJsonElement($call)"
        }
    }

    private fun generateFactory(plugins: List<PluginDescriptor>): String = buildString {
        appendLine(HEADER)
        appendLine("package $GENERATED_PACKAGE")
        appendLine()
        for (plugin in plugins) {
            appendLine("import ${plugin.qualifiedName}")
        }
        appendLine("import org.convos.bridge.runtime.PluginHandler")
        appendLine()
        appendLine("/** Builds the full handler list; one argument per @BridgePlugin so none can be forgotten. */")
        appendLine("fun bridgeHandlers(")
        for (plugin in plugins) {
            appendLine("    ${plugin.pluginName}: ${plugin.interfaceName},")
        }
        appendLine("): List<PluginHandler> = listOf(")
        for (plugin in plugins) {
            appendLine("    ${plugin.interfaceName}Handler(${plugin.pluginName}),")
        }
        appendLine(")")
    }

    private fun writeKotlinFile(fileName: String, content: String) {
        codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true),
            packageName = GENERATED_PACKAGE,
            fileName = fileName,
            extensionName = "kt",
        ).bufferedWriter().use { it.write(content.trimEnd('\n') + "\n") }
    }

    companion object {
        const val GENERATED_PACKAGE = "org.convos.bridge.generated"
        private const val HEADER = "// AUTOGENERATED by the convos bridge codegen processor. Do not edit."
    }
}
