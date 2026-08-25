package org.convos.metrics.descriptors.core

import org.convos.metrics.annotations.CoreActionsTarget
import org.convos.metrics.descriptors.navigation.PaywallSource

enum class ConversationSource {
    URL,
    Scan,
    Paste,
    Message
}

// Why an invite join ended without the user reaching the conversation. The
// client already classifies every one of these to choose error copy; carrying
// that classification into the funnel is what turns a bare success/failure
// split into something diagnosable. Null on a successful join.
enum class JoinFailureReason {
    // The creator's device never approved within the client wait window - the
    // user watched "Verifying" until it elapsed.
    APPROVAL_TIMED_OUT,
    // The join never got underway: the messaging inbox was still starting when
    // the invite arrived and never became ready, so the user watched
    // "Verifying" with no request in flight.
    INBOX_NEVER_READY,
    // Rejections the creator's device reported back over the join DM.
    INVITE_EXPIRED,
    CONVERSATION_EXPIRED,
    CONVERSATION_NOT_FOUND,
    CONSENT_NOT_ALLOWED,
    // Local validation of the scanned, pasted, or opened invite.
    INVALID_CODE_FORMAT,
    SIGNATURE_VERIFICATION_FAILED,
    // Transport failures, split the way the client already classifies them.
    NETWORK_SERVICE_UNAVAILABLE,
    NETWORK_TIMED_OUT,
    NETWORK_CONNECTION_LOST,
    NETWORK_TLS_FAILURE,
    // libxmtp storage and sequence errors surfaced by the state machine.
    INTERNAL_STORAGE_ERROR,
    UNKNOWN,
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

    // Fired when the user commits to an invite join, before any network work
    // begins. The funnel's denominator: joinedConversation reports how the
    // attempt ended, so a start with no matching outcome is itself the signal
    // that the client dropped the join without telling anyone.
    suspend fun joinAttemptStarted(
        source: ConversationSource
    )

    // Terminal event for an invite-join attempt. `isSuccess` distinguishes a
    // completed join from any failure - an approval that never came, a
    // rejection from the creator's device, or a local validation or transport
    // error. `verificationDuration` is the seconds the user spent waiting in
    // every case. `memberCount` / `hasAssistant` describe the joined
    // conversation and are null on failure (the user never entered it).
    // `failureReason` is null on success and otherwise names the cause;
    // `creatorReason` carries the creator device's own diagnostic string when
    // the rejection arrived over the join DM, and is null otherwise.
    // `attemptNumber` is 1 on the first try and increments per retry, so a
    // cause that only shows up on retries stays separable from a first-try one.
    suspend fun joinedConversation(
        verificationDuration: Float,
        memberCount: Int?,
        hasAssistant: Boolean?,
        source: ConversationSource,
        isSuccess: Boolean,
        failureReason: JoinFailureReason?,
        creatorReason: String?,
        attemptNumber: Int
    )

    // Fired when the client mints an invite for a conversation. Note this is
    // invite *creation* during conversation setup, not a user handing an
    // invite to someone - `sharedConversation` is that event. `isSuccess` is
    // false when minting threw, which previously only reached a local log.
    suspend fun invitedToConversation(
        memberCount: Int,
        hasAssistant: Boolean,
        isSuccess: Boolean
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
