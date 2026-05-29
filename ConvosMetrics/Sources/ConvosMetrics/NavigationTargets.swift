public enum NewConversationMode {
    case create
    case scanner
    case joinInvite
}

public enum PaywallSource {
    case settings
    case lowBalanceBanner
    case onboarding
    case memberCard
    case debug
}

public struct ConversationsNavigatorArgs {
    public init() {}
}

public struct ConversationNavigatorArgs {
    public let conversationId: String

    public init(conversationId: String) {
        self.conversationId = conversationId
    }
}

public struct AppSettingsNavigatorArgs {
    public init() {}
}

public struct NewConversationNavigatorArgs {
    public let mode: NewConversationMode
    public let inviteCode: String?

    public init(mode: NewConversationMode, inviteCode: String? = nil) {
        self.mode = mode
        self.inviteCode = inviteCode
    }
}

public struct ExplodeConfirmationNavigatorArgs {
    public let conversationId: String

    public init(conversationId: String) {
        self.conversationId = conversationId
    }
}

public struct ConversationInfoNavigatorArgs {
    public let conversationId: String

    public init(conversationId: String) {
        self.conversationId = conversationId
    }
}

public struct ConversationInfoEditNavigatorArgs {
    public let conversationId: String

    public init(conversationId: String) {
        self.conversationId = conversationId
    }
}

public struct MembersListNavigatorArgs {
    public let conversationId: String

    public init(conversationId: String) {
        self.conversationId = conversationId
    }
}

public struct MemberProfileNavigatorArgs {
    public let conversationId: String
    public let memberId: String

    public init(conversationId: String, memberId: String) {
        self.conversationId = conversationId
        self.memberId = memberId
    }
}

public struct ShareInviteNavigatorArgs {
    public let conversationId: String

    public init(conversationId: String) {
        self.conversationId = conversationId
    }
}

public struct ReactionsNavigatorArgs {
    public let conversationId: String
    public let messageId: String

    public init(conversationId: String, messageId: String) {
        self.conversationId = conversationId
        self.messageId = messageId
    }
}

public struct AssistantFilesLinksNavigatorArgs {
    public let conversationId: String

    public init(conversationId: String) {
        self.conversationId = conversationId
    }
}

public struct SetupProfileNavigatorArgs {
    public init() {}
}

public struct InviteAcceptedNavigatorArgs {
    public init() {}
}

public struct RequestPushNotificationsNavigatorArgs {
    public init() {}
}

public struct MyInfoNavigatorArgs {
    public init() {}
}

public struct CustomizeSettingsNavigatorArgs {
    public init() {}
}

public struct AssistantSettingsNavigatorArgs {
    public init() {}
}

public struct ConnectionsNavigatorArgs {
    public init() {}
}

public struct BackupRestoreNavigatorArgs {
    public init() {}
}

public struct DeleteAllDataNavigatorArgs {
    public init() {}
}

public struct QuicknameRandomizerNavigatorArgs {
    public init() {}
}

public struct InviteCodeEntryNavigatorArgs {
    public init() {}
}

public struct ConnectionGrantNavigatorArgs {
    public let serviceId: String
    public let conversationId: String

    public init(serviceId: String, conversationId: String) {
        self.serviceId = serviceId
        self.conversationId = conversationId
    }
}

public struct ExplodeInfoNavigatorArgs {
    public init() {}
}

public struct PinLimitInfoNavigatorArgs {
    public init() {}
}

public struct LockedConvoInfoNavigatorArgs {
    public let conversationId: String

    public init(conversationId: String) {
        self.conversationId = conversationId
    }
}

public struct FullConvoInfoNavigatorArgs {
    public init() {}
}

public struct ConversationForkedInfoNavigatorArgs {
    public let conversationId: String

    public init(conversationId: String) {
        self.conversationId = conversationId
    }
}

public struct RevealMediaInfoNavigatorArgs {
    public init() {}
}

public struct PhotosInfoNavigatorArgs {
    public init() {}
}

public struct AssistantConfirmationNavigatorArgs {
    public let conversationId: String

    public init(conversationId: String) {
        self.conversationId = conversationId
    }
}

public struct AssistantInfoNavigatorArgs {
    public init() {}
}

public struct ProcessingPowerInfoNavigatorArgs {
    public init() {}
}

public struct ExplodedInviteInfoNavigatorArgs {
    public init() {}
}

public struct BackwardsSecrecyInfoNavigatorArgs {
    public init() {}
}

public struct LockConvoConfirmationNavigatorArgs {
    public let conversationId: String

    public init(conversationId: String) {
        self.conversationId = conversationId
    }
}

public struct ContactsNavigatorArgs {
    public init() {}
}

public struct ContactCardNavigatorArgs {
    public let inboxId: String
    public let conversationId: String?

    public init(inboxId: String, conversationId: String? = nil) {
        self.inboxId = inboxId
        self.conversationId = conversationId
    }
}

public struct AgentTemplateContactCardNavigatorArgs {
    public let templateId: String
    public let inboxId: String
    public let conversationId: String?

    public init(templateId: String, inboxId: String, conversationId: String? = nil) {
        self.templateId = templateId
        self.inboxId = inboxId
        self.conversationId = conversationId
    }
}

public struct AddMembersNavigatorArgs {
    public let conversationId: String
    public let conversationTitle: String?

    public init(conversationId: String, conversationTitle: String? = nil) {
        self.conversationId = conversationId
        self.conversationTitle = conversationTitle
    }
}

public struct AgentBuilderNavigatorArgs {
    public let conversationId: String

    public init(conversationId: String) {
        self.conversationId = conversationId
    }
}

public struct ThinkingDetailNavigatorArgs {
    public let conversationId: String
    public let senderInboxId: String
    public let messageId: String

    public init(conversationId: String, senderInboxId: String, messageId: String) {
        self.conversationId = conversationId
        self.senderInboxId = senderInboxId
        self.messageId = messageId
    }
}

public struct HtmlAttachmentPreviewNavigatorArgs {
    public let conversationId: String?
    public let senderInboxId: String?

    public init(conversationId: String? = nil, senderInboxId: String? = nil) {
        self.conversationId = conversationId
        self.senderInboxId = senderInboxId
    }
}

public struct PaywallNavigatorArgs {
    public let source: PaywallSource

    public init(source: PaywallSource) {
        self.source = source
    }
}

public struct SubscriptionSettingsNavigatorArgs {
    public init() {}
}

public struct BillingDebugNavigatorArgs {
    public init() {}
}

public protocol ConversationsNavigator: AnyObject {
    func navigateTo(conversation: ConversationNavigatorArgs)
    func present(appSettings: AppSettingsNavigatorArgs)
    func present(newConversation: NewConversationNavigatorArgs)
    func present(explodeConfirmation: ExplodeConfirmationNavigatorArgs)
    func present(connectionGrant: ConnectionGrantNavigatorArgs)
    func present(explodeInfo: ExplodeInfoNavigatorArgs)
    func present(pinLimitInfo: PinLimitInfoNavigatorArgs)
    func present(contactCard: ContactCardNavigatorArgs)
    func present(agentBuilder: AgentBuilderNavigatorArgs)
    func closed(context: ScreenContext)
}

public protocol ConversationNavigator: AnyObject {
    func present(paywall: PaywallNavigatorArgs)
    func present(conversationInfo: ConversationInfoNavigatorArgs)
    func present(myInfo: MyInfoNavigatorArgs)
    func present(memberProfile: MemberProfileNavigatorArgs)
    func present(shareInvite: ShareInviteNavigatorArgs)
    func present(newConversation: NewConversationNavigatorArgs)
    func present(reactions: ReactionsNavigatorArgs)
    func present(explodeInfo: ExplodeInfoNavigatorArgs)
    func present(lockedConvoInfo: LockedConvoInfoNavigatorArgs)
    func present(fullConvoInfo: FullConvoInfoNavigatorArgs)
    func present(conversationForkedInfo: ConversationForkedInfoNavigatorArgs)
    func present(revealMediaInfo: RevealMediaInfoNavigatorArgs)
    func present(photosInfo: PhotosInfoNavigatorArgs)
    func present(assistantConfirmation: AssistantConfirmationNavigatorArgs)
    func present(assistantInfo: AssistantInfoNavigatorArgs)
    func present(processingPowerInfo: ProcessingPowerInfoNavigatorArgs)
    func present(explodedInviteInfo: ExplodedInviteInfoNavigatorArgs)
    func present(setupProfile: SetupProfileNavigatorArgs)
    func present(inviteAccepted: InviteAcceptedNavigatorArgs)
    func present(requestPushNotifications: RequestPushNotificationsNavigatorArgs)
    func present(backwardsSecrecyInfo: BackwardsSecrecyInfoNavigatorArgs)
    func present(addMembers: AddMembersNavigatorArgs)
    func present(contactCard: ContactCardNavigatorArgs)
    func present(agentTemplateContactCard: AgentTemplateContactCardNavigatorArgs)
    func present(agentBuilder: AgentBuilderNavigatorArgs)
    func present(thinkingDetail: ThinkingDetailNavigatorArgs)
    func present(htmlAttachmentPreview: HtmlAttachmentPreviewNavigatorArgs)
    func closed(context: ScreenContext)
}

public protocol AppSettingsNavigator: AnyObject {
    func navigateTo(myInfo: MyInfoNavigatorArgs)
    func navigateTo(customize: CustomizeSettingsNavigatorArgs)
    func navigateTo(assistants: AssistantSettingsNavigatorArgs)
    func navigateTo(connections: ConnectionsNavigatorArgs)
    func navigateTo(backupRestore: BackupRestoreNavigatorArgs)
    func navigateTo(deleteAllData: DeleteAllDataNavigatorArgs)
    func navigateTo(subscriptionSettings: SubscriptionSettingsNavigatorArgs)
    func navigateTo(contacts: ContactsNavigatorArgs)
    func closed(context: ScreenContext)
}

public protocol NewConversationNavigator: AnyObject {
    func navigateTo(conversation: ConversationNavigatorArgs)
    func closed(context: ScreenContext)
}

public protocol ExplodeConfirmationNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol ConversationInfoNavigator: AnyObject {
    func navigateTo(edit: ConversationInfoEditNavigatorArgs)
    func navigateTo(membersList: MembersListNavigatorArgs)
    func navigateTo(filesAndLinks: AssistantFilesLinksNavigatorArgs)
    func navigateTo(agentTemplateContactCard: AgentTemplateContactCardNavigatorArgs)
    func closed(context: ScreenContext)
}

public protocol ConversationInfoEditNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol MembersListNavigator: AnyObject {
    func navigateTo(memberProfile: MemberProfileNavigatorArgs)
    func navigateTo(agentTemplateContactCard: AgentTemplateContactCardNavigatorArgs)
    func closed(context: ScreenContext)
}

public protocol MemberProfileNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol ShareInviteNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol ReactionsNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol AssistantFilesLinksNavigator: AnyObject {
    func present(htmlAttachmentPreview: HtmlAttachmentPreviewNavigatorArgs)
    func closed(context: ScreenContext)
}

public protocol SetupProfileNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol InviteAcceptedNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol RequestPushNotificationsNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol MyInfoNavigator: AnyObject {
    func navigateTo(quicknameRandomizer: QuicknameRandomizerNavigatorArgs)
    func closed(context: ScreenContext)
}

public protocol CustomizeSettingsNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol AssistantSettingsNavigator: AnyObject {
    func present(inviteCodeEntry: InviteCodeEntryNavigatorArgs)
    func closed(context: ScreenContext)
}

public protocol ConnectionsNavigator: AnyObject {
    func present(connectionGrant: ConnectionGrantNavigatorArgs)
    func closed(context: ScreenContext)
}

public protocol BackupRestoreNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol DeleteAllDataNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol QuicknameRandomizerNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol InviteCodeEntryNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol ConnectionGrantNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol ExplodeInfoNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol PinLimitInfoNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol LockedConvoInfoNavigator: AnyObject {
    func present(lockConfirmation: LockConvoConfirmationNavigatorArgs)
    func closed(context: ScreenContext)
}

public protocol FullConvoInfoNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol ConversationForkedInfoNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol RevealMediaInfoNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol PhotosInfoNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol AssistantConfirmationNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol AssistantInfoNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol ProcessingPowerInfoNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol ExplodedInviteInfoNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol BackwardsSecrecyInfoNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol LockConvoConfirmationNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol ContactsNavigator: AnyObject {
    func navigateTo(contactCard: ContactCardNavigatorArgs)
    func present(newConversation: NewConversationNavigatorArgs)
    func closed(context: ScreenContext)
}

public protocol ContactCardNavigator: AnyObject {
    func navigateTo(contacts: ContactsNavigatorArgs)
    func closed(context: ScreenContext)
}

public protocol AgentTemplateContactCardNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol AddMembersNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol AgentBuilderNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol ThinkingDetailNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol HtmlAttachmentPreviewNavigator: AnyObject {
    func navigateTo(contactCard: ContactCardNavigatorArgs)
    func navigateTo(agentTemplateContactCard: AgentTemplateContactCardNavigatorArgs)
    func closed(context: ScreenContext)
}

public protocol PaywallNavigator: AnyObject {
    func closed(context: ScreenContext)
}

public protocol SubscriptionSettingsNavigator: AnyObject {
    func present(paywall: PaywallNavigatorArgs)
    func closed(context: ScreenContext)
}

public protocol BillingDebugNavigator: AnyObject {
    func present(paywall: PaywallNavigatorArgs)
    func navigateTo(subscriptionSettings: SubscriptionSettingsNavigatorArgs)
    func closed(context: ScreenContext)
}
