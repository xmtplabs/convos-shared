package org.convos.metrics.codegen.core

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import org.convos.metrics.codegen.util.toSnakeCase

class CoreExtractor(private val logger: KSPLogger) {

    fun extract(resolver: Resolver): CoreMetricsModel {
        val enumTypes = mutableMapOf<String, CoreEnumType>()

        val userProperties = resolver
            .getSymbolsWithAnnotation(USER_PROPERTIES_ANNOTATION)
            .filterIsInstance<KSClassDeclaration>()
            .toList()
            .also { warnIfMultiple(it, "@UserPropertiesTarget") }
            .firstOrNull()
            ?.let { extractUserProperties(it, enumTypes) }

        val coreActions = resolver
            .getSymbolsWithAnnotation(CORE_ACTIONS_ANNOTATION)
            .filterIsInstance<KSClassDeclaration>()
            .toList()
            .also { warnIfMultiple(it, "@CoreActionsTarget") }
            .firstOrNull()
            ?.let { extractCoreActions(it, enumTypes) }

        return CoreMetricsModel(
            userProperties = userProperties,
            coreActions = coreActions,
            enumTypes = enumTypes.values.toList(),
        )
    }

    private fun warnIfMultiple(decls: List<KSClassDeclaration>, label: String) {
        if (decls.size > 1) {
            logger.warn("Multiple $label declarations found; using ${decls.first().simpleName.asString()}")
        }
    }

    private fun extractUserProperties(
        classDecl: KSClassDeclaration,
        enumTypes: MutableMap<String, CoreEnumType>,
    ): UserPropertiesDescriptor {
        val properties = classDecl.primaryConstructor?.parameters
            ?.map { it.toCoreProperty(enumTypes) }
            ?: emptyList()

        return UserPropertiesDescriptor(
            name = classDecl.simpleName.asString(),
            qualifiedName = classDecl.qualifiedName?.asString() ?: classDecl.simpleName.asString(),
            properties = properties,
        )
    }

    private fun extractCoreActions(
        classDecl: KSClassDeclaration,
        enumTypes: MutableMap<String, CoreEnumType>,
    ): CoreActionsDescriptor {
        val actions = classDecl.getDeclaredFunctions()
            .map { extractAction(it, enumTypes) }
            .toList()

        return CoreActionsDescriptor(
            name = classDecl.simpleName.asString(),
            qualifiedName = classDecl.qualifiedName?.asString() ?: classDecl.simpleName.asString(),
            actions = actions,
        )
    }

    private fun extractAction(
        function: KSFunctionDeclaration,
        enumTypes: MutableMap<String, CoreEnumType>,
    ): CoreAction {
        val name = function.simpleName.asString()
        return CoreAction(
            name = name,
            eventName = name.toSnakeCase(),
            parameters = function.parameters.map { it.toCoreProperty(enumTypes) },
            isSuspend = function.modifiers.contains(Modifier.SUSPEND),
        )
    }

    private fun KSValueParameter.toCoreProperty(
        enumTypes: MutableMap<String, CoreEnumType>,
    ): CoreProperty {
        val type = type.resolve()
        val decl = type.declaration
        val paramName = name?.asString() ?: ""

        var isEnum = false
        if (decl is KSClassDeclaration && decl.classKind == ClassKind.ENUM_CLASS) {
            isEnum = true
            val enumName = decl.simpleName.asString()
            enumTypes.getOrPut(enumName) {
                CoreEnumType(
                    name = enumName,
                    qualifiedName = decl.qualifiedName?.asString() ?: enumName,
                    values = decl.declarations
                        .filterIsInstance<KSClassDeclaration>()
                        .filter { it.classKind == ClassKind.ENUM_ENTRY }
                        .map {
                            val raw = it.simpleName.asString()
                            CoreEnumValue(name = raw, snakeName = raw.toSnakeCase())
                        }
                        .toList(),
                )
            }
        }

        var elementType: String? = null
        var elementQualifiedType: String? = null
        if (decl.qualifiedName?.asString() == "kotlin.collections.List") {
            val arg = type.arguments.firstOrNull()?.type?.resolve()
            val argDecl = arg?.declaration
            if (argDecl != null) {
                elementType = argDecl.simpleName.asString()
                elementQualifiedType = argDecl.qualifiedName?.asString() ?: ""
            }
        }

        return CoreProperty(
            name = paramName,
            snakeName = paramName.toSnakeCase(),
            type = decl.simpleName.asString(),
            qualifiedType = decl.qualifiedName?.asString() ?: "",
            nullable = type.isMarkedNullable,
            isEnum = isEnum,
            elementType = elementType,
            elementQualifiedType = elementQualifiedType,
        )
    }

    companion object {
        private const val USER_PROPERTIES_ANNOTATION =
            "org.convos.metrics.annotations.UserPropertiesTarget"
        private const val CORE_ACTIONS_ANNOTATION =
            "org.convos.metrics.annotations.CoreActionsTarget"
    }
}
