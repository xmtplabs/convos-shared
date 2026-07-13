public final class MetricsCoreActions: CoreActions, @unchecked Sendable {
    private weak var delegate: CollectorDelegate?

    public init(delegate: CollectorDelegate) {
        self.delegate = delegate
    }

    public func startedConversation() async {
        delegate?.sendEvent(name: Self.eventStartedConversation, properties: [:])
    }

    public func joinedConversation(verificationDuration: Float, memberCount: Int?, hasAssistant: Bool?, source: ConversationSource, isSuccess: Bool) async {
        delegate?.sendEvent(name: Self.eventJoinedConversation, properties: [
            Self.paramVerificationDuration: verificationDuration,
            Self.paramMemberCount: memberCount,
            Self.paramHasAssistant: hasAssistant,
            Self.paramSource: source.metricsString,
            Self.paramIsSuccess: isSuccess,
        ])
    }

    public func invitedToConversation(memberCount: Int, hasAssistant: Bool) async {
        delegate?.sendEvent(name: Self.eventInvitedToConversation, properties: [
            Self.paramMemberCount: memberCount,
            Self.paramHasAssistant: hasAssistant,
        ])
    }

    public func addedAssistant(memberCount: Int) async {
        delegate?.sendEvent(name: Self.eventAddedAssistant, properties: [
            Self.paramMemberCount: memberCount,
        ])
    }

    public func assistantJoined(waitDuration: Float, source: AssistantJoinSource, memberCount: Int?, isSuccess: Bool) async {
        delegate?.sendEvent(name: Self.eventAssistantJoined, properties: [
            Self.paramWaitDuration: waitDuration,
            Self.paramSource: source.metricsString,
            Self.paramMemberCount: memberCount,
            Self.paramIsSuccess: isSuccess,
        ])
    }

    public func assistantJoinRescuedByPolling(streamAgeSecs: Float, pollTick: Int) async {
        delegate?.sendEvent(name: Self.eventAssistantJoinRescuedByPolling, properties: [
            Self.paramStreamAgeSecs: streamAgeSecs,
            Self.paramPollTick: pollTick,
        ])
    }

    public func sentMessage(sendingTime: Float, memberCount: Int, attachmentTypes: [String], hasText: Bool, hasAssistant: Bool, isSuccess: Bool) async {
        delegate?.sendEvent(name: Self.eventSentMessage, properties: [
            Self.paramSendingTime: sendingTime,
            Self.paramMemberCount: memberCount,
            Self.paramAttachmentTypes: attachmentTypes,
            Self.paramHasText: hasText,
            Self.paramHasAssistant: hasAssistant,
            Self.paramIsSuccess: isSuccess,
        ])
    }

    public func sharedConversation(memberCount: Int, hasAssistant: Bool, shareTarget: ShareTarget, hasExpiration: Bool, expiresAfterUse: Bool, isSuccess: Bool) async {
        delegate?.sendEvent(name: Self.eventSharedConversation, properties: [
            Self.paramMemberCount: memberCount,
            Self.paramHasAssistant: hasAssistant,
            Self.paramShareTarget: shareTarget.metricsString,
            Self.paramHasExpiration: hasExpiration,
            Self.paramExpiresAfterUse: expiresAfterUse,
            Self.paramIsSuccess: isSuccess,
        ])
    }

    public func builtAgent(buildDuration: Float, instructionCharCount: Int, instructionWordCount: Int, attachmentTypes: [String], hasVoiceMemo: Bool, voiceMemoDuration: Float, connectionTypes: [String], entryMode: AgentBuilderEntryMode, isSuccess: Bool, fromPromptHint: Bool, tapCount: Int) async {
        delegate?.sendEvent(name: Self.eventBuiltAgent, properties: [
            Self.paramBuildDuration: buildDuration,
            Self.paramInstructionCharCount: instructionCharCount,
            Self.paramInstructionWordCount: instructionWordCount,
            Self.paramAttachmentTypes: attachmentTypes,
            Self.paramHasVoiceMemo: hasVoiceMemo,
            Self.paramVoiceMemoDuration: voiceMemoDuration,
            Self.paramConnectionTypes: connectionTypes,
            Self.paramEntryMode: entryMode.metricsString,
            Self.paramIsSuccess: isSuccess,
            Self.paramFromPromptHint: fromPromptHint,
            Self.paramTapCount: tapCount,
        ])
    }

    public func promptHintTapped(tapCount: Int) async {
        delegate?.sendEvent(name: Self.eventPromptHintTapped, properties: [
            Self.paramTapCount: tapCount,
        ])
    }

    public func purchaseInitiated(productId: String, tier: SubscriptionTier, period: SubscriptionPeriod, source: PaywallSource) async {
        delegate?.sendEvent(name: Self.eventPurchaseInitiated, properties: [
            Self.paramProductId: productId,
            Self.paramTier: tier.metricsString,
            Self.paramPeriod: period.metricsString,
            Self.paramSource: source.metricsString,
        ])
    }

    public func purchaseSucceeded(productId: String, tier: SubscriptionTier, period: SubscriptionPeriod, source: PaywallSource, durationSecs: Float) async {
        delegate?.sendEvent(name: Self.eventPurchaseSucceeded, properties: [
            Self.paramProductId: productId,
            Self.paramTier: tier.metricsString,
            Self.paramPeriod: period.metricsString,
            Self.paramSource: source.metricsString,
            Self.paramDurationSecs: durationSecs,
        ])
    }

    public func purchaseCancelled(productId: String, source: PaywallSource) async {
        delegate?.sendEvent(name: Self.eventPurchaseCancelled, properties: [
            Self.paramProductId: productId,
            Self.paramSource: source.metricsString,
        ])
    }

    public func purchaseFailed(productId: String, source: PaywallSource, reason: PurchaseFailureReason) async {
        delegate?.sendEvent(name: Self.eventPurchaseFailed, properties: [
            Self.paramProductId: productId,
            Self.paramSource: source.metricsString,
            Self.paramReason: reason.metricsString,
        ])
    }

    public func purchasesRestored(restoredCount: Int) async {
        delegate?.sendEvent(name: Self.eventPurchasesRestored, properties: [
            Self.paramRestoredCount: restoredCount,
        ])
    }

    public func devicePairingStarted(role: DevicePairingRole) async {
        delegate?.sendEvent(name: Self.eventDevicePairingStarted, properties: [
            Self.paramRole: role.metricsString,
        ])
    }

    public func devicePairingCompleted(role: DevicePairingRole, durationSecs: Float) async {
        delegate?.sendEvent(name: Self.eventDevicePairingCompleted, properties: [
            Self.paramRole: role.metricsString,
            Self.paramDurationSecs: durationSecs,
        ])
    }

    public func devicePairingFailed(role: DevicePairingRole, reason: DevicePairingFailureReason, step: DevicePairingStep, durationSecs: Float) async {
        delegate?.sendEvent(name: Self.eventDevicePairingFailed, properties: [
            Self.paramRole: role.metricsString,
            Self.paramReason: reason.metricsString,
            Self.paramStep: step.metricsString,
            Self.paramDurationSecs: durationSecs,
        ])
    }

    public static let eventStartedConversation: String = "started_conversation"
    public static let eventJoinedConversation: String = "joined_conversation"
    public static let eventInvitedToConversation: String = "invited_to_conversation"
    public static let eventAddedAssistant: String = "added_assistant"
    public static let eventAssistantJoined: String = "assistant_joined"
    public static let eventAssistantJoinRescuedByPolling: String = "assistant_join_rescued_by_polling"
    public static let eventSentMessage: String = "sent_message"
    public static let eventSharedConversation: String = "shared_conversation"
    public static let eventBuiltAgent: String = "built_agent"
    public static let eventPromptHintTapped: String = "prompt_hint_tapped"
    public static let eventPurchaseInitiated: String = "purchase_initiated"
    public static let eventPurchaseSucceeded: String = "purchase_succeeded"
    public static let eventPurchaseCancelled: String = "purchase_cancelled"
    public static let eventPurchaseFailed: String = "purchase_failed"
    public static let eventPurchasesRestored: String = "purchases_restored"
    public static let eventDevicePairingStarted: String = "device_pairing_started"
    public static let eventDevicePairingCompleted: String = "device_pairing_completed"
    public static let eventDevicePairingFailed: String = "device_pairing_failed"
    public static let paramVerificationDuration: String = "verification_duration"
    public static let paramMemberCount: String = "member_count"
    public static let paramHasAssistant: String = "has_assistant"
    public static let paramSource: String = "source"
    public static let paramIsSuccess: String = "is_success"
    public static let paramWaitDuration: String = "wait_duration"
    public static let paramStreamAgeSecs: String = "stream_age_secs"
    public static let paramPollTick: String = "poll_tick"
    public static let paramSendingTime: String = "sending_time"
    public static let paramAttachmentTypes: String = "attachment_types"
    public static let paramHasText: String = "has_text"
    public static let paramShareTarget: String = "share_target"
    public static let paramHasExpiration: String = "has_expiration"
    public static let paramExpiresAfterUse: String = "expires_after_use"
    public static let paramBuildDuration: String = "build_duration"
    public static let paramInstructionCharCount: String = "instruction_char_count"
    public static let paramInstructionWordCount: String = "instruction_word_count"
    public static let paramHasVoiceMemo: String = "has_voice_memo"
    public static let paramVoiceMemoDuration: String = "voice_memo_duration"
    public static let paramConnectionTypes: String = "connection_types"
    public static let paramEntryMode: String = "entry_mode"
    public static let paramFromPromptHint: String = "from_prompt_hint"
    public static let paramTapCount: String = "tap_count"
    public static let paramProductId: String = "product_id"
    public static let paramTier: String = "tier"
    public static let paramPeriod: String = "period"
    public static let paramDurationSecs: String = "duration_secs"
    public static let paramReason: String = "reason"
    public static let paramRestoredCount: String = "restored_count"
    public static let paramRole: String = "role"
    public static let paramStep: String = "step"
}
