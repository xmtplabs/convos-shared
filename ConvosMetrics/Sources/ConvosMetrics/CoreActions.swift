public protocol CoreActions: AnyObject, Sendable {
    func startedConversation() async
    func joinedConversation(verificationDuration: Float, memberCount: Int, hasAssistant: Bool, source: ConversationSource) async
    func invitedToConversation(memberCount: Int, hasAssistant: Bool) async
    func addedAssistant(memberCount: Int) async
    func assistantJoined(waitDuration: Float, surface: AssistantJoinSurface, memberCount: Int) async
    func assistantJoinTimedOut(waitDuration: Float, surface: AssistantJoinSurface) async
    func assistantJoinRescuedByPolling(streamAgeSecs: Float, pollTick: Int) async
    func sentMessage(sendingTime: Float, memberCount: Int, attachmentTypes: [String], hasText: Bool, hasAssistant: Bool, isSuccess: Bool) async
    func sharedConversation(memberCount: Int, hasAssistant: Bool, shareTarget: ShareTarget, hasExpiration: Bool, expiresAfterUse: Bool, isSuccess: Bool) async
    func builtAgent(buildDuration: Float, instructionCharCount: Int, instructionWordCount: Int, attachmentTypes: [String], hasVoiceMemo: Bool, voiceMemoDuration: Float, connectionTypes: [String], entryMode: AgentBuilderEntryMode, isSuccess: Bool) async
    func purchaseInitiated(productId: String, tier: SubscriptionTier, period: SubscriptionPeriod, source: PaywallSource) async
    func purchaseSucceeded(productId: String, tier: SubscriptionTier, period: SubscriptionPeriod, source: PaywallSource, durationSecs: Float) async
    func purchaseCancelled(productId: String, source: PaywallSource) async
    func purchaseFailed(productId: String, source: PaywallSource, reason: PurchaseFailureReason) async
    func purchasesRestored(restoredCount: Int) async
}
