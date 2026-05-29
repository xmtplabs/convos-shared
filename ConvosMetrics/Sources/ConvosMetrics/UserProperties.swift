public struct UserProperties: Sendable {
    public let hasMessagedAssistant: Bool
    public let lastAssistantMessageTimestamp: String?
    public let contactCount: Int
    public let conversationCount: Int
    public let assistantConversationCount: Int
    public let conversationCount24Hours: Int
    public let conversationCount7Days: Int
    public let maxActiveConvoAge: Float

    public init(
        hasMessagedAssistant: Bool,
        lastAssistantMessageTimestamp: String? = nil,
        contactCount: Int,
        conversationCount: Int,
        assistantConversationCount: Int,
        conversationCount24Hours: Int,
        conversationCount7Days: Int,
        maxActiveConvoAge: Float
    ) {
        self.hasMessagedAssistant = hasMessagedAssistant
        self.lastAssistantMessageTimestamp = lastAssistantMessageTimestamp
        self.contactCount = contactCount
        self.conversationCount = conversationCount
        self.assistantConversationCount = assistantConversationCount
        self.conversationCount24Hours = conversationCount24Hours
        self.conversationCount7Days = conversationCount7Days
        self.maxActiveConvoAge = maxActiveConvoAge
    }
}
