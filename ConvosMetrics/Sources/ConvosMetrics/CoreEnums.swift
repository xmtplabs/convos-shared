public enum ConversationSource {
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

public enum SubscriptionTier {
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

public enum SubscriptionPeriod {
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

public enum PurchaseFailureReason {
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
