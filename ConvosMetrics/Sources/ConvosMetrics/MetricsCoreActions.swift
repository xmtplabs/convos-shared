public final class MetricsCoreActions: CoreActions, @unchecked Sendable {
    private weak var delegate: CollectorDelegate?

    public init(delegate: CollectorDelegate) {
        self.delegate = delegate
    }

    public func startedConversation() async {
        delegate?.sendEvent(name: Self.eventStartedConversation, properties: [:])
    }

    public func joinedConversation(verificationDuration: Float, memberCount: Int, hasAssistant: Bool, source: ConversationSource) async {
        delegate?.sendEvent(name: Self.eventJoinedConversation, properties: [
            Self.paramVerificationDuration: verificationDuration,
            Self.paramMemberCount: memberCount,
            Self.paramHasAssistant: hasAssistant,
            Self.paramSource: source.metricsString,
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

    public func lowBalanceBannerShown(isDepleted: Bool) async {
        delegate?.sendEvent(name: Self.eventLowBalanceBannerShown, properties: [
            Self.paramIsDepleted: isDepleted,
        ])
    }

    public static let eventStartedConversation: String = "started_conversation"
    public static let eventJoinedConversation: String = "joined_conversation"
    public static let eventInvitedToConversation: String = "invited_to_conversation"
    public static let eventAddedAssistant: String = "added_assistant"
    public static let eventSentMessage: String = "sent_message"
    public static let eventPurchaseInitiated: String = "purchase_initiated"
    public static let eventPurchaseSucceeded: String = "purchase_succeeded"
    public static let eventPurchaseCancelled: String = "purchase_cancelled"
    public static let eventPurchaseFailed: String = "purchase_failed"
    public static let eventPurchasesRestored: String = "purchases_restored"
    public static let eventLowBalanceBannerShown: String = "low_balance_banner_shown"
    public static let paramVerificationDuration: String = "verification_duration"
    public static let paramMemberCount: String = "member_count"
    public static let paramHasAssistant: String = "has_assistant"
    public static let paramSource: String = "source"
    public static let paramSendingTime: String = "sending_time"
    public static let paramAttachmentTypes: String = "attachment_types"
    public static let paramHasText: String = "has_text"
    public static let paramIsSuccess: String = "is_success"
    public static let paramProductId: String = "product_id"
    public static let paramTier: String = "tier"
    public static let paramPeriod: String = "period"
    public static let paramDurationSecs: String = "duration_secs"
    public static let paramReason: String = "reason"
    public static let paramRestoredCount: String = "restored_count"
    public static let paramIsDepleted: String = "is_depleted"
}
