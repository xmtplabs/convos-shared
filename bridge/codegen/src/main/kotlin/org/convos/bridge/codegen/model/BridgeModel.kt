package org.convos.bridge.codegen.model

/** Everything the generators need, extracted from the `@BridgePlugin` interfaces. */
data class BridgeModel(
    val plugins: List<PluginDescriptor>,
    val structs: List<StructDescriptor>,
    val enums: List<EnumDescriptor>,
) {
    val isEmpty: Boolean get() = plugins.isEmpty()
}

data class PluginDescriptor(
    /** Kotlin interface simple name, e.g. `MembershipPlugin`. */
    val interfaceName: String,
    val qualifiedName: String,
    /** Wire name used in `method` envelopes, e.g. `membership`. */
    val pluginName: String,
    val functions: List<FunctionDescriptor>,
)

data class FunctionDescriptor(
    val name: String,
    val parameters: List<PropertyDescriptor>,
    val returnType: TypeRef,
    val isSuspend: Boolean,
) {
    /** A non-suspend function returning `Flow<T>` is a native->JS event stream. */
    val isEvent: Boolean get() = returnType.kind == TypeKind.FLOW

    /** Non-suspend Unit functions (that are not events) are fire-and-forget notifications. */
    val isNotification: Boolean get() = !isSuspend && !isEvent
}

data class PropertyDescriptor(
    val name: String,
    val type: TypeRef,
)

enum class TypeKind { STRING, INT, LONG, BOOLEAN, DOUBLE, FLOAT, UNIT, ENUM, STRUCT, LIST, FLOW }

data class TypeRef(
    val kind: TypeKind,
    /** Simple name of the declaration (`Profile`, `MemberRole`, `String`, ...). */
    val name: String,
    val qualifiedName: String,
    val nullable: Boolean,
    /** Element type when [kind] is [TypeKind.LIST] or [TypeKind.FLOW]. */
    val element: TypeRef? = null,
)

/** A `@Serializable` data class carried across the bridge. */
data class StructDescriptor(
    val name: String,
    val qualifiedName: String,
    val properties: List<PropertyDescriptor>,
)

data class EnumDescriptor(
    val name: String,
    val qualifiedName: String,
    /** Entry names in declaration order; also the wire values. */
    val values: List<String>,
)
