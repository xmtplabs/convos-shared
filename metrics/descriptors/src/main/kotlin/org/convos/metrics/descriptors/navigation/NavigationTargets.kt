package org.convos.metrics.descriptors.navigation

import org.convos.metrics.annotations.NavigationTarget

// Primary Navigators

@NavigationTarget
interface ConversationsNavigator {
    class Args

    fun navigateTo(conversation: ConversationNavigator.Args)
    fun present(appSettings: AppSettingsNavigator.Args)
    fun present(newConversation: NewConversationNavigator.Args)
    fun present(explodeConfirmation: ExplodeConfirmationNavigator.Args)
    fun present(connectionGrant: ConnectionGrantNavigator.Args)
    fun present(explodeInfo: ExplodeInfoNavigator.Args)
    fun present(pinLimitInfo: PinLimitInfoNavigator.Args)

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface ConversationNavigator {
    data class Args(val conversationId: String)

    fun present(paywall: PaywallNavigator.Args)
    fun present(conversationInfo: ConversationInfoNavigator.Args)
    fun present(myInfo: MyInfoNavigator.Args)
    fun present(memberProfile: MemberProfileNavigator.Args)
    fun present(shareInvite: ShareInviteNavigator.Args)
    fun present(newConversation: NewConversationNavigator.Args)
    fun present(reactions: ReactionsNavigator.Args)
    fun present(explodeInfo: ExplodeInfoNavigator.Args)
    fun present(lockedConvoInfo: LockedConvoInfoNavigator.Args)
    fun present(fullConvoInfo: FullConvoInfoNavigator.Args)
    fun present(conversationForkedInfo: ConversationForkedInfoNavigator.Args)
    fun present(revealMediaInfo: RevealMediaInfoNavigator.Args)
    fun present(photosInfo: PhotosInfoNavigator.Args)
    fun present(assistantConfirmation: AssistantConfirmationNavigator.Args)
    fun present(assistantInfo: AssistantInfoNavigator.Args)
    fun present(processingPowerInfo: ProcessingPowerInfoNavigator.Args)
    fun present(explodedInviteInfo: ExplodedInviteInfoNavigator.Args)
    fun present(setupProfile: SetupProfileNavigator.Args)
    fun present(inviteAccepted: InviteAcceptedNavigator.Args)
    fun present(requestPushNotifications: RequestPushNotificationsNavigator.Args)
    fun present(backwardsSecrecyInfo: BackwardsSecrecyInfoNavigator.Args)

    fun closed(context: ScreenContext)
}

// Root-Level Modals

@NavigationTarget
interface AppSettingsNavigator {
    class Args

    fun navigateTo(myInfo: MyInfoNavigator.Args)
    fun navigateTo(customize: CustomizeSettingsNavigator.Args)
    fun navigateTo(assistants: AssistantSettingsNavigator.Args)
    fun navigateTo(connections: ConnectionsNavigator.Args)
    fun navigateTo(backupRestore: BackupRestoreNavigator.Args)
    fun navigateTo(deleteAllData: DeleteAllDataNavigator.Args)
    fun navigateTo(subscriptionSettings: SubscriptionSettingsNavigator.Args)

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface NewConversationNavigator {
    data class Args(
        val mode: NewConversationMode,
        val inviteCode: String? = null,
    )

    fun closed(context: ScreenContext)
}

enum class NewConversationMode {
    CREATE,
    SCANNER,
    JOIN_INVITE,
}

@NavigationTarget
interface ExplodeConfirmationNavigator {
    data class Args(val conversationId: String)

    fun closed(context: ScreenContext)
}

// Conversation Detail Modals

@NavigationTarget
interface ConversationInfoNavigator {
    data class Args(val conversationId: String)

    fun navigateTo(edit: ConversationInfoEditNavigator.Args)
    fun navigateTo(membersList: MembersListNavigator.Args)
    fun navigateTo(filesAndLinks: AssistantFilesLinksNavigator.Args)

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface ConversationInfoEditNavigator {
    data class Args(val conversationId: String)

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface MembersListNavigator {
    data class Args(val conversationId: String)

    fun navigateTo(memberProfile: MemberProfileNavigator.Args)

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface MemberProfileNavigator {
    data class Args(
        val conversationId: String,
        val memberId: String,
    )

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface ShareInviteNavigator {
    data class Args(val conversationId: String)

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface ReactionsNavigator {
    data class Args(
        val conversationId: String,
        val messageId: String,
    )

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface AssistantFilesLinksNavigator {
    data class Args(val conversationId: String)

    fun closed(context: ScreenContext)
}

// Conversation Onboarding

@NavigationTarget
interface SetupProfileNavigator {
    class Args

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface InviteAcceptedNavigator {
    class Args

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface RequestPushNotificationsNavigator {
    class Args

    fun closed(context: ScreenContext)
}

// Settings Sub-Navigators

@NavigationTarget
interface MyInfoNavigator {
    class Args

    fun navigateTo(quicknameRandomizer: QuicknameRandomizerNavigator.Args)

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface CustomizeSettingsNavigator {
    class Args

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface AssistantSettingsNavigator {
    class Args

    fun present(inviteCodeEntry: InviteCodeEntryNavigator.Args)

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface ConnectionsNavigator {
    class Args

    fun present(connectionGrant: ConnectionGrantNavigator.Args)

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface BackupRestoreNavigator {
    class Args

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface DeleteAllDataNavigator {
    class Args

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface QuicknameRandomizerNavigator {
    class Args

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface InviteCodeEntryNavigator {
    class Args

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface ConnectionGrantNavigator {
    data class Args(
        val serviceId: String,
        val conversationId: String,
    )

    fun closed(context: ScreenContext)
}

// Info Sheets

@NavigationTarget
interface ExplodeInfoNavigator {
    class Args

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface PinLimitInfoNavigator {
    class Args

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface LockedConvoInfoNavigator {
    data class Args(val conversationId: String)

    fun present(lockConfirmation: LockConvoConfirmationNavigator.Args)

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface FullConvoInfoNavigator {
    class Args

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface ConversationForkedInfoNavigator {
    data class Args(val conversationId: String)

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface RevealMediaInfoNavigator {
    class Args

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface PhotosInfoNavigator {
    class Args

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface AssistantConfirmationNavigator {
    data class Args(val conversationId: String)

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface AssistantInfoNavigator {
    class Args

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface ProcessingPowerInfoNavigator {
    class Args

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface ExplodedInviteInfoNavigator {
    class Args

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface BackwardsSecrecyInfoNavigator {
    class Args

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface MaxedOutInfoNavigator {
    class Args

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface LockConvoConfirmationNavigator {
    data class Args(val conversationId: String)

    fun closed(context: ScreenContext)
}

// Paywall & Billing

@NavigationTarget
interface PaywallNavigator {
    data class Args(val source: PaywallSource)

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface SubscriptionSettingsNavigator {
    class Args

    fun present(paywall: PaywallNavigator.Args)

    fun closed(context: ScreenContext)
}

@NavigationTarget
interface BillingDebugNavigator {
    class Args

    fun present(paywall: PaywallNavigator.Args)
    fun navigateTo(subscriptionSettings: SubscriptionSettingsNavigator.Args)

    fun closed(context: ScreenContext)
}

enum class PaywallSource {
    SETTINGS,
    LOW_BALANCE_BANNER,
    ONBOARDING,
    MEMBER_CARD,
    DEBUG,
}
