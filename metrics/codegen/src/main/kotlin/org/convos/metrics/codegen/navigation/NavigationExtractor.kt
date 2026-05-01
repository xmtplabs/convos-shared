package org.convos.metrics.codegen.navigation

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier

class NavigationExtractor(private val logger: KSPLogger) {

    fun extract(resolver: Resolver): NavigationGraph {
        val symbols = resolver
            .getSymbolsWithAnnotation(NAVIGATION_TARGET_ANNOTATION)
            .filterIsInstance<KSClassDeclaration>()

        val targets = mutableMapOf<String, NavigationTargetDescriptor>()
        val edges = mutableListOf<NavigationEdge>()
        val enumTypes = mutableMapOf<String, NavigationEnumType>()

        for (classDecl in symbols) {
            val descriptor = extractTarget(classDecl, enumTypes)
            targets[descriptor.name] = descriptor

            for (method in descriptor.navigationMethods) {
                edges.add(
                    NavigationEdge(
                        source = descriptor.name,
                        target = method.targetScreen,
                        methodType = method.methodType,
                        parameterName = method.parameterName,
                    )
                )
            }
        }

        logger.info("Extracted ${targets.size} navigation targets with ${edges.size} edges")
        return NavigationGraph(targets, edges, enumTypes.values.toList())
    }

    private fun extractTarget(
        classDecl: KSClassDeclaration,
        enumTypes: MutableMap<String, NavigationEnumType>,
    ): NavigationTargetDescriptor {
        val name = classDecl.simpleName.asString()
        val metricsName = name.toSnakeCase()
        val qualifiedName = classDecl.qualifiedName?.asString() ?: name

        val argsClass = classDecl.declarations
            .filterIsInstance<KSClassDeclaration>()
            .firstOrNull { it.simpleName.asString() == "Args" }

        val args = argsClass?.let { extractArgs(it, enumTypes) } ?: NavigationArgs.EMPTY

        val methods = classDecl.getDeclaredFunctions()
            .mapNotNull { extractMethod(it) }
            .toList()

        return NavigationTargetDescriptor(
            name = name,
            metricsName = metricsName,
            qualifiedName = qualifiedName,
            args = args,
            methods = methods,
        )
    }

    private fun extractArgs(
        argsClass: KSClassDeclaration,
        enumTypes: MutableMap<String, NavigationEnumType>,
    ): NavigationArgs {
        val isDataClass = argsClass.modifiers.contains(Modifier.DATA)
        val parameters = argsClass.primaryConstructor?.parameters?.map { param ->
            val type = param.type.resolve()
            val decl = type.declaration

            if (decl is KSClassDeclaration && decl.classKind == ClassKind.ENUM_CLASS) {
                val enumName = decl.simpleName.asString()
                enumTypes.getOrPut(enumName) {
                    NavigationEnumType(
                        name = enumName,
                        qualifiedName = decl.qualifiedName?.asString() ?: enumName,
                        values = decl.declarations
                            .filterIsInstance<KSClassDeclaration>()
                            .filter { it.classKind == ClassKind.ENUM_ENTRY }
                            .map { it.simpleName.asString() }
                            .toList(),
                    )
                }
            }

            NavigationParameter(
                name = param.name?.asString() ?: "",
                type = type.declaration.simpleName.asString(),
                qualifiedType = type.declaration.qualifiedName?.asString() ?: "",
                nullable = type.isMarkedNullable,
                hasDefault = param.hasDefault,
            )
        } ?: emptyList()

        return NavigationArgs(parameters, isDataClass)
    }

    private fun extractMethod(function: KSFunctionDeclaration): NavigationMethod? {
        val methodName = function.simpleName.asString()

        val methodType = when (methodName) {
            "navigateTo" -> NavigationMethodType.NAVIGATE_TO
            "present" -> NavigationMethodType.PRESENT
            "closed" -> NavigationMethodType.CLOSED
            else -> {
                logger.warn("Unknown navigation method: $methodName")
                return null
            }
        }

        if (methodType == NavigationMethodType.CLOSED) {
            return NavigationMethod(
                parameterName = "",
                methodType = methodType,
                targetScreen = "",
                targetQualifiedName = "",
            )
        }

        val param = function.parameters.firstOrNull() ?: run {
            logger.warn("Navigation method $methodName has no parameters")
            return null
        }
        val paramName = param.name?.asString() ?: return null
        val paramType = param.type.resolve()

        // The parameter type is ScreenName.Args — resolve the enclosing screen class
        val argsDecl = paramType.declaration
        val screenDecl = argsDecl.parentDeclaration as? KSClassDeclaration

        val targetScreen = screenDecl?.simpleName?.asString() ?: ""
        val targetQualifiedName = screenDecl?.qualifiedName?.asString() ?: ""

        return NavigationMethod(
            parameterName = paramName,
            methodType = methodType,
            targetScreen = targetScreen,
            targetQualifiedName = targetQualifiedName,
        )
    }

    companion object {
        private const val NAVIGATION_TARGET_ANNOTATION =
            "org.convos.metrics.annotations.NavigationTarget"

        private fun String.toSnakeCase(): String =
            removeSuffix("Navigator")
                .replace(Regex("([a-z])([A-Z])"), "$1_$2")
                .replace(Regex("([A-Z])([A-Z][a-z])"), "$1_$2")
                .lowercase()
    }
}
