package org.convos.metrics.descriptors.core

import org.convos.metrics.annotations.UserPropertiesTarget

@UserPropertiesTarget
data class UserProperties(
    val hasMessagedAssistant: Boolean = false,
    val lastAssistantMessageTimestamp: String? = null,
    val contactCount: Int = 0,
    val conversationCount: Int = 0,
    val assistantConversationCount: Int = 0,
    val conversationCount24Hours: Int = 0,
    val conversationCount7Days: Int = 0,
    val maxActiveConvoAge: Float = 0f, // seconds
)
