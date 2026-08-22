package org.convos.bridge.codegen.model

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import org.convos.bridge.codegen.util.pluginWireName

/**
 * Builds the [BridgeModel] from every `@BridgePlugin` interface, validating the
 * conventions the generators rely on. Validation failures are reported through
 * [KSPLogger.error], which fails the build.
 */
class BridgeExtractor(private val logger: KSPLogger) {

    private val structs = linkedMapOf<String, StructDescriptor>()
    private val enums = linkedMapOf<String, EnumDescriptor>()

    fun extract(resolver: Resolver): BridgeModel {
        val plugins = resolver
            .getSymbolsWithAnnotation(BRIDGE_PLUGIN_ANNOTATION)
            .filterIsInstance<KSClassDeclaration>()
            .mapNotNull { extractPlugin(it) }
            .sortedBy { it.pluginName }
            .toList()

        validateUniqueNames(plugins)

        return BridgeModel(
            plugins = plugins,
            structs = structs.values.toList(),
            enums = enums.values.toList(),
        )
    }

    private fun extractPlugin(decl: KSClassDeclaration): PluginDescriptor? {
        val name = decl.simpleName.asString()
        if (decl.classKind != ClassKind.INTERFACE) {
            logger.error("@BridgePlugin must annotate an interface", decl)
            return null
        }
        if (!name.endsWith("Plugin") || name == "Plugin") {
            logger.error("@BridgePlugin interface name must end in 'Plugin' ($name)", decl)
            return null
        }

        val functions = decl.getDeclaredFunctions()
            .filter { it.simpleName.asString() != "<init>" }
            .mapNotNull { extractFunction(it) }
            .toList()

        return PluginDescriptor(
            interfaceName = name,
            qualifiedName = decl.qualifiedName?.asString() ?: name,
            pluginName = pluginWireName(name),
            functions = functions,
        )
    }

    private fun extractFunction(function: KSFunctionDeclaration): FunctionDescriptor? {
        val name = function.simpleName.asString()
        val isSuspend = function.modifiers.contains(Modifier.SUSPEND)

        val returnType = function.returnType?.resolve()
            ?.let { toTypeRef(it, function) }
            ?: TypeRef(TypeKind.UNIT, "Unit", "kotlin.Unit", nullable = false)

        if (returnType.kind == TypeKind.FLOW) {
            if (isSuspend) {
                logger.error("Bridge event function '$name' must not be suspend (return Flow<T> directly)", function)
                return null
            }
            if (function.parameters.isNotEmpty()) {
                logger.error("Bridge event function '$name' must take no parameters", function)
                return null
            }
        } else if (!isSuspend && returnType.kind != TypeKind.UNIT) {
            logger.error(
                "Bridge function '$name' must be suspend, return Unit, or return Flow<T> " +
                    "(non-suspend Unit functions are notifications; Flow<T> functions are events)",
                function,
            )
            return null
        }
        if (returnType.nullable && returnType.kind == TypeKind.UNIT) {
            logger.error("Bridge function '$name' must not return Unit?", function)
            return null
        }

        val parameters = function.parameters.map { param ->
            val paramName = param.name?.asString() ?: run {
                logger.error("Unnamed parameter on bridge function '$name'", function)
                return null
            }
            val type = toTypeRef(param.type.resolve(), function) ?: return null
            PropertyDescriptor(paramName, type)
        }

        return FunctionDescriptor(
            name = name,
            parameters = parameters,
            returnType = returnType,
            isSuspend = isSuspend,
        )
    }

    private fun toTypeRef(type: KSType, at: KSNode): TypeRef? {
        val decl = type.declaration
        val qualified = decl.qualifiedName?.asString() ?: decl.simpleName.asString()
        val nullable = type.isMarkedNullable

        BUILTINS[qualified]?.let { kind ->
            return TypeRef(kind, decl.simpleName.asString(), qualified, nullable)
        }

        if (qualified == "kotlin.collections.List") {
            val elementType = type.arguments.firstOrNull()?.type?.resolve() ?: run {
                logger.error("Cannot resolve List element type", at)
                return null
            }
            val element = toTypeRef(elementType, at) ?: return null
            return TypeRef(TypeKind.LIST, "List", qualified, nullable, element = element)
        }

        if (qualified == "kotlinx.coroutines.flow.Flow") {
            val elementType = type.arguments.firstOrNull()?.type?.resolve() ?: run {
                logger.error("Cannot resolve Flow element type", at)
                return null
            }
            val element = toTypeRef(elementType, at) ?: return null
            return TypeRef(TypeKind.FLOW, "Flow", qualified, nullable, element = element)
        }

        if (decl !is KSClassDeclaration) {
            logger.error("Unsupported bridge type '$qualified'", at)
            return null
        }

        return when (decl.classKind) {
            ClassKind.ENUM_CLASS -> {
                registerEnum(decl)
                TypeRef(TypeKind.ENUM, decl.simpleName.asString(), qualified, nullable)
            }
            ClassKind.CLASS -> {
                if (!registerStruct(decl, at)) return null
                TypeRef(TypeKind.STRUCT, decl.simpleName.asString(), qualified, nullable)
            }
            else -> {
                logger.error("Unsupported bridge type '$qualified' (${decl.classKind})", at)
                null
            }
        }
    }

    private fun registerEnum(decl: KSClassDeclaration) {
        val qualified = decl.qualifiedName?.asString() ?: decl.simpleName.asString()
        enums.getOrPut(qualified) {
            EnumDescriptor(
                name = decl.simpleName.asString(),
                qualifiedName = qualified,
                values = decl.declarations
                    .filterIsInstance<KSClassDeclaration>()
                    .filter { it.classKind == ClassKind.ENUM_ENTRY }
                    .map { it.simpleName.asString() }
                    .toList(),
            )
        }
    }

    /** Registers a `@Serializable` data class (recursing into its properties). */
    private fun registerStruct(decl: KSClassDeclaration, at: KSNode): Boolean {
        val qualified = decl.qualifiedName?.asString() ?: decl.simpleName.asString()
        if (qualified in structs) return true

        if (decl.annotations.none { isSerializableAnnotation(it.annotationType.resolve()) }) {
            logger.error("Bridge type '$qualified' must be annotated with @kotlinx.serialization.Serializable", at)
            return false
        }
        val constructor = decl.primaryConstructor ?: run {
            logger.error("Bridge type '$qualified' must have a primary constructor", at)
            return false
        }

        // Reserve the slot before recursing so type cycles terminate.
        structs[qualified] = StructDescriptor(decl.simpleName.asString(), qualified, emptyList())

        val properties = constructor.parameters.map { param ->
            val name = param.name?.asString() ?: run {
                logger.error("Unnamed constructor parameter on '$qualified'", decl)
                return false
            }
            val type = toTypeRef(param.type.resolve(), decl) ?: return false
            PropertyDescriptor(name, type)
        }

        structs[qualified] = StructDescriptor(decl.simpleName.asString(), qualified, properties)
        return true
    }

    private fun isSerializableAnnotation(type: KSType): Boolean =
        type.declaration.qualifiedName?.asString() == "kotlinx.serialization.Serializable"

    /**
     * The JS surface is flat (`window.convos.getProfile(...)`), so function
     * names must be unique across all plugins; plugin wire names must be
     * unique too.
     */
    private fun validateUniqueNames(plugins: List<PluginDescriptor>) {
        plugins.groupBy { it.pluginName }.values
            .filter { it.size > 1 }
            .forEach { collided ->
                logger.error(
                    "Duplicate plugin wire name '${collided.first().pluginName}': " +
                        collided.joinToString { it.interfaceName },
                )
            }

        val functionOwners = mutableMapOf<String, String>()
        for (plugin in plugins) {
            for (function in plugin.functions) {
                val previous = functionOwners.put(function.name, plugin.interfaceName)
                if (previous != null) {
                    logger.error(
                        "Duplicate bridge function name '${function.name}' in $previous and " +
                            "${plugin.interfaceName}; convos.js exposes a flat namespace",
                    )
                }
                if (function.name == "onReady" || function.name.startsWith("_")) {
                    logger.error(
                        "Bridge function name '${function.name}' collides with the reserved " +
                            "convos.js surface (onReady / _-prefixed members)",
                    )
                }
            }
        }
    }

    companion object {
        private const val BRIDGE_PLUGIN_ANNOTATION = "org.convos.bridge.annotations.BridgePlugin"

        private val BUILTINS = mapOf(
            "kotlin.String" to TypeKind.STRING,
            "kotlin.Int" to TypeKind.INT,
            "kotlin.Long" to TypeKind.LONG,
            "kotlin.Boolean" to TypeKind.BOOLEAN,
            "kotlin.Double" to TypeKind.DOUBLE,
            "kotlin.Float" to TypeKind.FLOAT,
            "kotlin.Unit" to TypeKind.UNIT,
        )
    }
}
