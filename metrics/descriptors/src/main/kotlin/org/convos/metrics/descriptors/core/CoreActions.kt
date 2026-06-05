package org.convos.metrics.descriptors.core

import org.convos.metrics.annotations.CoreActionsTarget
import org.convos.metrics.descriptors.navigation.PaywallSource

enum class ConversationSource {
    URL,
    Scan,
    Paste,
    Message
}

enum class SubscriptionTier {
    BUILDER,
    PRO,
}

enum class SubscriptionPeriod {
    MONTHLY,
    ANNUAL,
}

enum class ShareTarget {
    MESSAGES,
    MAIL,
    COPY,
    QR_CODE,
    AIRDROP,
    OTHER,
    CANCELLED,
}

enum class AgentBuilderEntryMode {
    COMPOSER,
    VOICE_MEMO,
}

enum class PurchaseFailureReason {
    PRODUCT_NOT_FOUND,
    PURCHASE_PENDING,
    PURCHASE_UNVERIFIED,
    BACKEND_VERIFY_UNAVAILABLE,
    BILLING_CLIENT_UNAVAILABLE,
    UNKNOWN,
}

// The UI surface where the user watches an assistant join after requesting
// one: the in-chat pending status bubble (bare add), the agent contact card
// (template add / deep link), or the agent-builder placeholder (post-Make).
enum class AssistantJoinSurface {
    STATUS_MESSAGE,
    CONTACT_CARD,
    BUILDER_PLACEHOLDER,
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

    // Fired when a verified assistant actually appears in the conversation's
    // member list after a join was requested. `waitDuration` is the seconds
    // the user spent watching the joining/verifying state.
    suspend fun assistantJoined(
        waitDuration: Float,
        surface: AssistantJoinSurface,
        memberCount: Int
    )

    // Fired when no verified assistant appeared within the join wait window
    // (the assistant backend gives up after about two minutes).
    suspend fun assistantJoinTimedOut(
        waitDuration: Float,
        surface: AssistantJoinSurface
    )

    // Fired when the client's join-request polling fallback processed an
    // assistant join request that the realtime message stream had missed -
    // direct evidence of a silently dead stream. `streamAgeSecs` is the time
    // since the stream last delivered any message (-1 when it never did).
    suspend fun assistantJoinRescuedByPolling(
        streamAgeSecs: Float,
        pollTick: Int
    )

    suspend fun sentMessage(
        sendingTime: Float,
        memberCount: Int,
        attachmentTypes: List<String>,
        hasText: Boolean,
        hasAssistant: Boolean,
        isSuccess: Boolean
    )

    suspend fun sharedConversation(
        memberCount: Int,
        hasAssistant: Boolean,
        shareTarget: ShareTarget,
        hasExpiration: Boolean,
        expiresAfterUse: Boolean,
        isSuccess: Boolean
    )

    suspend fun builtAgent(
        buildDuration: Float,
        instructionCharCount: Int,
        instructionWordCount: Int,
        attachmentTypes: List<String>,
        hasVoiceMemo: Boolean,
        voiceMemoDuration: Float,
        connectionTypes: List<String>,
        entryMode: AgentBuilderEntryMode,
        isSuccess: Boolean
    )

    suspend fun purchaseInitiated(
        productId: String,
        tier: SubscriptionTier,
        period: SubscriptionPeriod,
        source: PaywallSource,
    )

    suspend fun purchaseSucceeded(
        productId: String,
        tier: SubscriptionTier,
        period: SubscriptionPeriod,
        source: PaywallSource,
        durationSecs: Float,
    )

    suspend fun purchaseCancelled(
        productId: String,
        source: PaywallSource,
    )

    suspend fun purchaseFailed(
        productId: String,
        source: PaywallSource,
        reason: PurchaseFailureReason,
    )

    suspend fun purchasesRestored(restoredCount: Int)
}
