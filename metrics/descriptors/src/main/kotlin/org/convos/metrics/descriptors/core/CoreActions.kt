package org.convos.metrics.descriptors.core

import org.convos.metrics.annotations.CoreActionsTarget

enum class ConversationSource {
    URL,
    Scan,
    Paste,
    Message
}

@CoreActionsTarget
interface CoreActions {
    suspend fun startedConversation()

    suspend fun joinedConversation(
        verificationDuration: Float,
        memberCount: Int,
        hasAssistant: Boolean,
        source: ConversationSource
    )

    suspend fun invitedToConversation(
        memberCount: Int,
        hasAssistant: Boolean
    )

    suspend fun addedAssistant(
        memberCount: Int
    )

    suspend fun sentMessage(
        sendingTime: Float,
        memberCount: Int,
        attachmentCount: Int,
        hasText: Boolean,
        hasAssistant: Boolean,
        isSuccess: Boolean
    )
}
