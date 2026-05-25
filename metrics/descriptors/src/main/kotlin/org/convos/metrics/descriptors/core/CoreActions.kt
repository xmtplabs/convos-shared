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

enum class PurchaseFailureReason {
    PRODUCT_NOT_FOUND,
    PURCHASE_PENDING,
    PURCHASE_UNVERIFIED,
    BACKEND_VERIFY_UNAVAILABLE,
    BILLING_CLIENT_UNAVAILABLE,
    UNKNOWN,
}

@CoreActionsTarget
interface CoreActions {
    suspend fun startedConversation()

    suspend fun joinedConversation(
        verificationDuration: Float,
        memberCount: Int,
        hasAssistant: Boolean,
        source: ConversationSource
    )

    suspend fun invitedToConversation(
        memberCount: Int,
        hasAssistant: Boolean
    )

    suspend fun addedAssistant(
        memberCount: Int
    )

    suspend fun sentMessage(
        sendingTime: Float,
        memberCount: Int,
        attachmentTypes: List<String>,
        hasText: Boolean,
        hasAssistant: Boolean,
        isSuccess: Boolean
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

    suspend fun lowBalanceBannerShown(isDepleted: Boolean)
}
