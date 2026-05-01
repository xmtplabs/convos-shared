package org.convos.metrics.codegen.navigation

import kotlinx.serialization.Serializable

@Serializable
enum class NavigationMethodType {
    NAVIGATE_TO,
    PRESENT,
    CLOSED,
}

@Serializable
data class NavigationParameter(
    val name: String,
    val type: String,
    val qualifiedType: String,
    val nullable: Boolean,
    val hasDefault: Boolean,
)

@Serializable
data class NavigationArgs(
    val parameters: List<NavigationParameter>,
    val isDataClass: Boolean,
) {
    companion object {
        val EMPTY = NavigationArgs(parameters = emptyList(), isDataClass = false)
    }
}

@Serializable
data class NavigationMethod(
    val parameterName: String,
    val methodType: NavigationMethodType,
    val targetScreen: String,
    val targetQualifiedName: String,
)

@Serializable
data class NavigationTargetDescriptor(
    val name: String,
    val metricsName: String,
    val qualifiedName: String,
    val args: NavigationArgs,
    val methods: List<NavigationMethod>,
) {
    val navigationMethods: List<NavigationMethod>
        get() = methods.filter { it.methodType != NavigationMethodType.CLOSED }

    val hasClosed: Boolean
        get() = methods.any { it.methodType == NavigationMethodType.CLOSED }
}

@Serializable
data class NavigationEnumType(
    val name: String,
    val qualifiedName: String,
    val values: List<String>,
)

@Serializable
data class NavigationEdge(
    val source: String,
    val target: String,
    val methodType: NavigationMethodType,
    val parameterName: String,
)

@Serializable
data class NavigationGraph(
    val targets: Map<String, NavigationTargetDescriptor>,
    val edges: List<NavigationEdge>,
    val enumTypes: List<NavigationEnumType> = emptyList(),
) {
    fun outgoingEdges(screenName: String): List<NavigationEdge> =
        edges.filter { it.source == screenName }

    fun incomingEdges(screenName: String): List<NavigationEdge> =
        edges.filter { it.target == screenName }

    fun leafScreens(): List<NavigationTargetDescriptor> =
        targets.values.filter { outgoingEdges(it.name).isEmpty() }

    fun rootScreens(): List<NavigationTargetDescriptor> =
        targets.values.filter { incomingEdges(it.name).isEmpty() }
}
