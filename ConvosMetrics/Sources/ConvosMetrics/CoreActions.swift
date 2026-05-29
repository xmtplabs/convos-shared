public protocol CoreActions: AnyObject, Sendable {
    func startedConversation() async
    func joinedConversation(verificationDuration: Float, memberCount: Int, hasAssistant: Bool, source: ConversationSource) async
    func invitedToConversation(memberCount: Int, hasAssistant: Bool) async
    func addedAssistant(memberCount: Int) async
    func sentMessage(sendingTime: Float, memberCount: Int, attachmentTypes: [String], hasText: Bool, hasAssistant: Bool, isSuccess: Bool) async
    func purchaseInitiated(productId: String, tier: SubscriptionTier, period: SubscriptionPeriod, source: PaywallSource) async
    func purchaseSucceeded(productId: String, tier: SubscriptionTier, period: SubscriptionPeriod, source: PaywallSource, durationSecs: Float) async
    func purchaseCancelled(productId: String, source: PaywallSource) async
    func purchaseFailed(productId: String, source: PaywallSource, reason: PurchaseFailureReason) async
    func purchasesRestored(restoredCount: Int) async
    func lowBalanceBannerShown(isDepleted: Bool) async
}
