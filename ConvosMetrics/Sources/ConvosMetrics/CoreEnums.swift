public enum ConversationSource: Sendable {
    case url
    case scan
    case paste
    case message
}

extension ConversationSource {
    public var metricsString: String {
        switch self {
        case .url: return "url"
        case .scan: return "scan"
        case .paste: return "paste"
        case .message: return "message"
        }
    }
}

public enum JoinFailureReason: Sendable {
    case approvalTimedOut
    case inboxNeverReady
    case inviteExpired
    case conversationExpired
    case conversationNotFound
    case consentNotAllowed
    case invalidCodeFormat
    case signatureVerificationFailed
    case networkServiceUnavailable
    case networkTimedOut
    case networkConnectionLost
    case networkTlsFailure
    case internalStorageError
    case unknown
}

extension JoinFailureReason {
    public var metricsString: String {
        switch self {
        case .approvalTimedOut: return "approval_timed_out"
        case .inboxNeverReady: return "inbox_never_ready"
        case .inviteExpired: return "invite_expired"
        case .conversationExpired: return "conversation_expired"
        case .conversationNotFound: return "conversation_not_found"
        case .consentNotAllowed: return "consent_not_allowed"
        case .invalidCodeFormat: return "invalid_code_format"
        case .signatureVerificationFailed: return "signature_verification_failed"
        case .networkServiceUnavailable: return "network_service_unavailable"
        case .networkTimedOut: return "network_timed_out"
        case .networkConnectionLost: return "network_connection_lost"
        case .networkTlsFailure: return "network_tls_failure"
        case .internalStorageError: return "internal_storage_error"
        case .unknown: return "unknown"
        }
    }
}

public enum AssistantJoinSource: Sendable {
    case addToConversation
    case agentTemplate
    case agentBuilder
}

extension AssistantJoinSource {
    public var metricsString: String {
        switch self {
        case .addToConversation: return "add_to_conversation"
        case .agentTemplate: return "agent_template"
        case .agentBuilder: return "agent_builder"
        }
    }
}

public enum ShareTarget: Sendable {
    case messages
    case mail
    case copy
    case qrCode
    case airdrop
    case other
    case cancelled
}

extension ShareTarget {
    public var metricsString: String {
        switch self {
        case .messages: return "messages"
        case .mail: return "mail"
        case .copy: return "copy"
        case .qrCode: return "qr_code"
        case .airdrop: return "airdrop"
        case .other: return "other"
        case .cancelled: return "cancelled"
        }
    }
}

public enum AgentBuilderEntryMode: Sendable {
    case composer
    case voiceMemo
}

extension AgentBuilderEntryMode {
    public var metricsString: String {
        switch self {
        case .composer: return "composer"
        case .voiceMemo: return "voice_memo"
        }
    }
}

public enum SubscriptionTier: Sendable {
    case builder
    case pro
}

extension SubscriptionTier {
    public var metricsString: String {
        switch self {
        case .builder: return "builder"
        case .pro: return "pro"
        }
    }
}

public enum SubscriptionPeriod: Sendable {
    case monthly
    case annual
}

extension SubscriptionPeriod {
    public var metricsString: String {
        switch self {
        case .monthly: return "monthly"
        case .annual: return "annual"
        }
    }
}

extension PaywallSource {
    public var metricsString: String {
        switch self {
        case .settings: return "settings"
        case .lowBalanceBanner: return "low_balance_banner"
        case .onboarding: return "onboarding"
        case .memberCard: return "member_card"
        case .debug: return "debug"
        }
    }
}

public enum PurchaseFailureReason: Sendable {
    case productNotFound
    case purchasePending
    case purchaseUnverified
    case backendVerifyUnavailable
    case billingClientUnavailable
    case unknown
}

extension PurchaseFailureReason {
    public var metricsString: String {
        switch self {
        case .productNotFound: return "product_not_found"
        case .purchasePending: return "purchase_pending"
        case .purchaseUnverified: return "purchase_unverified"
        case .backendVerifyUnavailable: return "backend_verify_unavailable"
        case .billingClientUnavailable: return "billing_client_unavailable"
        case .unknown: return "unknown"
        }
    }
}
