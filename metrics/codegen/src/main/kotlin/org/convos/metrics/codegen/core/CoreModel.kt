package org.convos.metrics.codegen.core

import kotlinx.serialization.Serializable

@Serializable
data class CoreEnumValue(
    val name: String,
    val snakeName: String,
)

@Serializable
data class CoreEnumType(
    val name: String,
    val qualifiedName: String,
    val values: List<CoreEnumValue>,
)

@Serializable
data class CoreProperty(
    val name: String,
    val snakeName: String,
    val type: String,
    val qualifiedType: String,
    val nullable: Boolean,
    val isEnum: Boolean = false,
)

@Serializable
data class UserPropertiesDescriptor(
    val name: String,
    val qualifiedName: String,
    val properties: List<CoreProperty>,
)

@Serializable
data class CoreAction(
    val name: String,
    val eventName: String,
    val parameters: List<CoreProperty>,
    val isSuspend: Boolean = false,
)

@Serializable
data class CoreActionsDescriptor(
    val name: String,
    val qualifiedName: String,
    val actions: List<CoreAction>,
)

@Serializable
data class CoreMetricsModel(
    val userProperties: UserPropertiesDescriptor? = null,
    val coreActions: CoreActionsDescriptor? = null,
    val enumTypes: List<CoreEnumType> = emptyList(),
) {
    val isEmpty: Boolean
        get() = userProperties == null && coreActions == null
}
