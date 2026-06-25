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

// How the user initiated an assistant join - the stable entry-point intent,
// captured at request time and carried through to the join moment. Defined by
// intent (not by the UI surface it happens to render on) so the metric stays
// comparable across UI redesigns, mirroring ConversationSource.
enum class AssistantJoinSource {
    // Bare "add an assistant" affordance in a conversation, plus the
    // multi-select contacts picker.
    ADD_TO_CONVERSATION,
    // Agent template / share link / convos://template deep link.
    AGENT_TEMPLATE,
    // Agent builder, after the user taps Make.
    AGENT_BUILDER,
}

@CoreActionsTarget
interface CoreActions {
    suspend fun startedConversation()

    // Terminal event for an invite-join attempt. `isSuccess` distinguishes a
    // completed join from one that timed out in the "Verifying" state because
    // the conversation creator's device never approved the join request.
    // `verificationDuration` is the seconds the user spent waiting in both
    // cases. `memberCount` / `hasAssistant` describe the joined conversation
    // and are null on timeout (the user never entered it).
    suspend fun joinedConversation(
        verificationDuration: Float,
        memberCount: Int?,
        hasAssistant: Boolean?,
        source: ConversationSource,
        isSuccess: Boolean
    )

    suspend fun invitedToConversation(
        memberCount: Int,
        hasAssistant: Boolean
    )

    suspend fun addedAssistant(
        memberCount: Int
    )

    // Terminal event for an assistant-join attempt after the user requested
    // one. Fires at the stable join moment (assistant appears in the member
    // list) or at the wait-window timeout. `isSuccess` distinguishes the two
    // (the assistant backend gives up after about two minutes). `waitDuration`
    // is the seconds spent in the joining/verifying state; `source` is the
    // stable entry-point intent. `memberCount` is the joined conversation
    // size, null on timeout.
    suspend fun assistantJoined(
        waitDuration: Float,
        source: AssistantJoinSource,
        memberCount: Int?,
        isSuccess: Boolean
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
        isSuccess: Boolean,
        fromPromptHint: Boolean,
        tapCount: Int
    )

    suspend fun promptHintTapped(
        tapCount: Int
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
