import Foundation

public final class CoreMetrics: @unchecked Sendable {
    public let actions: CoreActions
    private weak var delegate: CollectorDelegate?
    private let stableId: MetricsStableIdEncoder

    public init(delegate: CollectorDelegate, stableId: MetricsStableIdEncoder) {
        self.delegate = delegate
        self.stableId = stableId
        self.actions = MetricsCoreActions(delegate: delegate)
    }

    public func identify(privateKey: Data) {
        delegate?.identify(userId: stableId.derive(privateKey: privateKey))
    }

    public func updateUserProperties(properties: UserProperties) async {
        delegate?.updateUserProperties(properties: [
            Self.userPropertyHasMessagedAssistant: properties.hasMessagedAssistant,
            Self.userPropertyLastAssistantMessageTimestamp: properties.lastAssistantMessageTimestamp,
            Self.userPropertyContactCount: properties.contactCount,
            Self.userPropertyConversationCount: properties.conversationCount,
            Self.userPropertyAssistantConversationCount: properties.assistantConversationCount,
            Self.userPropertyConversationCount24Hours: properties.conversationCount24Hours,
            Self.userPropertyConversationCount7Days: properties.conversationCount7Days,
            Self.userPropertyMaxActiveConvoAge: properties.maxActiveConvoAge,
        ])
    }

    public static let userPropertyHasMessagedAssistant: String = "has_messaged_assistant"
    public static let userPropertyLastAssistantMessageTimestamp: String = "last_assistant_message_timestamp"
    public static let userPropertyContactCount: String = "contact_count"
    public static let userPropertyConversationCount: String = "conversation_count"
    public static let userPropertyAssistantConversationCount: String = "assistant_conversation_count"
    public static let userPropertyConversationCount24Hours: String = "conversation_count24_hours"
    public static let userPropertyConversationCount7Days: String = "conversation_count7_days"
    public static let userPropertyMaxActiveConvoAge: String = "max_active_convo_age"
}
