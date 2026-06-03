public class TabRootCollector: TabRootNavigator {
    private weak var instance: TabRootNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: TabRootNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func navigateTo(conversations: ConversationsNavigatorArgs) {
        delegate?.navigatedTo(source: Self.name, target: ConversationsCollector.name)
        instance?.navigateTo(conversations: conversations)
    }

    public func navigateTo(stuffOverview: StuffOverviewNavigatorArgs) {
        delegate?.navigatedTo(source: Self.name, target: StuffOverviewCollector.name)
        instance?.navigateTo(stuffOverview: stuffOverview)
    }

    public func navigateTo(contacts: ContactsNavigatorArgs) {
        delegate?.navigatedTo(source: Self.name, target: ContactsCollector.name)
        instance?.navigateTo(contacts: contacts)
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "tab_root"
}

public class ConversationsCollector: ConversationsNavigator {
    private weak var instance: ConversationsNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: ConversationsNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func navigateTo(conversation: ConversationNavigatorArgs) {
        delegate?.navigatedTo(source: Self.name, target: ConversationCollector.name)
        instance?.navigateTo(conversation: conversation)
    }

    public func present(appSettings: AppSettingsNavigatorArgs) {
        delegate?.presented(source: Self.name, target: AppSettingsCollector.name)
        instance?.present(appSettings: appSettings)
    }

    public func present(newConversation: NewConversationNavigatorArgs) {
        delegate?.presented(source: Self.name, target: NewConversationCollector.name)
        instance?.present(newConversation: newConversation)
    }

    public func present(explodeConfirmation: ExplodeConfirmationNavigatorArgs) {
        delegate?.presented(source: Self.name, target: ExplodeConfirmationCollector.name)
        instance?.present(explodeConfirmation: explodeConfirmation)
    }

    public func present(connectionGrant: ConnectionGrantNavigatorArgs) {
        delegate?.presented(source: Self.name, target: ConnectionGrantCollector.name)
        instance?.present(connectionGrant: connectionGrant)
    }

    public func present(explodeInfo: ExplodeInfoNavigatorArgs) {
        delegate?.presented(source: Self.name, target: ExplodeInfoCollector.name)
        instance?.present(explodeInfo: explodeInfo)
    }

    public func present(pinLimitInfo: PinLimitInfoNavigatorArgs) {
        delegate?.presented(source: Self.name, target: PinLimitInfoCollector.name)
        instance?.present(pinLimitInfo: pinLimitInfo)
    }

    public func present(contactCard: ContactCardNavigatorArgs) {
        delegate?.presented(source: Self.name, target: ContactCardCollector.name)
        instance?.present(contactCard: contactCard)
    }

    public func present(agentBuilder: AgentBuilderNavigatorArgs) {
        delegate?.presented(source: Self.name, target: AgentBuilderCollector.name)
        instance?.present(agentBuilder: agentBuilder)
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "conversations"
}

public class ConversationCollector: ConversationNavigator {
    private weak var instance: ConversationNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: ConversationNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func present(paywall: PaywallNavigatorArgs) {
        delegate?.presented(source: Self.name, target: PaywallCollector.name)
        instance?.present(paywall: paywall)
    }

    public func present(conversationInfo: ConversationInfoNavigatorArgs) {
        delegate?.presented(source: Self.name, target: ConversationInfoCollector.name)
        instance?.present(conversationInfo: conversationInfo)
    }

    public func present(myInfo: MyInfoNavigatorArgs) {
        delegate?.presented(source: Self.name, target: MyInfoCollector.name)
        instance?.present(myInfo: myInfo)
    }

    public func present(memberProfile: MemberProfileNavigatorArgs) {
        delegate?.presented(source: Self.name, target: MemberProfileCollector.name)
        instance?.present(memberProfile: memberProfile)
    }

    public func present(shareInvite: ShareInviteNavigatorArgs) {
        delegate?.presented(source: Self.name, target: ShareInviteCollector.name)
        instance?.present(shareInvite: shareInvite)
    }

    public func present(newConversation: NewConversationNavigatorArgs) {
        delegate?.presented(source: Self.name, target: NewConversationCollector.name)
        instance?.present(newConversation: newConversation)
    }

    public func present(reactions: ReactionsNavigatorArgs) {
        delegate?.presented(source: Self.name, target: ReactionsCollector.name)
        instance?.present(reactions: reactions)
    }

    public func present(explodeInfo: ExplodeInfoNavigatorArgs) {
        delegate?.presented(source: Self.name, target: ExplodeInfoCollector.name)
        instance?.present(explodeInfo: explodeInfo)
    }

    public func present(lockedConvoInfo: LockedConvoInfoNavigatorArgs) {
        delegate?.presented(source: Self.name, target: LockedConvoInfoCollector.name)
        instance?.present(lockedConvoInfo: lockedConvoInfo)
    }

    public func present(fullConvoInfo: FullConvoInfoNavigatorArgs) {
        delegate?.presented(source: Self.name, target: FullConvoInfoCollector.name)
        instance?.present(fullConvoInfo: fullConvoInfo)
    }

    public func present(conversationForkedInfo: ConversationForkedInfoNavigatorArgs) {
        delegate?.presented(source: Self.name, target: ConversationForkedInfoCollector.name)
        instance?.present(conversationForkedInfo: conversationForkedInfo)
    }

    public func present(revealMediaInfo: RevealMediaInfoNavigatorArgs) {
        delegate?.presented(source: Self.name, target: RevealMediaInfoCollector.name)
        instance?.present(revealMediaInfo: revealMediaInfo)
    }

    public func present(photosInfo: PhotosInfoNavigatorArgs) {
        delegate?.presented(source: Self.name, target: PhotosInfoCollector.name)
        instance?.present(photosInfo: photosInfo)
    }

    public func present(assistantConfirmation: AssistantConfirmationNavigatorArgs) {
        delegate?.presented(source: Self.name, target: AssistantConfirmationCollector.name)
        instance?.present(assistantConfirmation: assistantConfirmation)
    }

    public func present(agentInfo: AgentInfoNavigatorArgs) {
        delegate?.presented(source: Self.name, target: AgentInfoCollector.name)
        instance?.present(agentInfo: agentInfo)
    }

    public func present(agentPowerInfo: AgentPowerInfoNavigatorArgs) {
        delegate?.presented(source: Self.name, target: AgentPowerInfoCollector.name)
        instance?.present(agentPowerInfo: agentPowerInfo)
    }

    public func present(explodedInviteInfo: ExplodedInviteInfoNavigatorArgs) {
        delegate?.presented(source: Self.name, target: ExplodedInviteInfoCollector.name)
        instance?.present(explodedInviteInfo: explodedInviteInfo)
    }

    public func present(setupProfile: SetupProfileNavigatorArgs) {
        delegate?.presented(source: Self.name, target: SetupProfileCollector.name)
        instance?.present(setupProfile: setupProfile)
    }

    public func present(inviteAccepted: InviteAcceptedNavigatorArgs) {
        delegate?.presented(source: Self.name, target: InviteAcceptedCollector.name)
        instance?.present(inviteAccepted: inviteAccepted)
    }

    public func present(requestPushNotifications: RequestPushNotificationsNavigatorArgs) {
        delegate?.presented(source: Self.name, target: RequestPushNotificationsCollector.name)
        instance?.present(requestPushNotifications: requestPushNotifications)
    }

    public func present(backwardsSecrecyInfo: BackwardsSecrecyInfoNavigatorArgs) {
        delegate?.presented(source: Self.name, target: BackwardsSecrecyInfoCollector.name)
        instance?.present(backwardsSecrecyInfo: backwardsSecrecyInfo)
    }

    public func present(addMembers: AddMembersNavigatorArgs) {
        delegate?.presented(source: Self.name, target: AddMembersCollector.name)
        instance?.present(addMembers: addMembers)
    }

    public func present(contactCard: ContactCardNavigatorArgs) {
        delegate?.presented(source: Self.name, target: ContactCardCollector.name)
        instance?.present(contactCard: contactCard)
    }

    public func present(agentTemplateContactCard: AgentTemplateContactCardNavigatorArgs) {
        delegate?.presented(source: Self.name, target: AgentTemplateContactCardCollector.name)
        instance?.present(agentTemplateContactCard: agentTemplateContactCard)
    }

    public func present(agentBuilder: AgentBuilderNavigatorArgs) {
        delegate?.presented(source: Self.name, target: AgentBuilderCollector.name)
        instance?.present(agentBuilder: agentBuilder)
    }

    public func present(thinkingDetail: ThinkingDetailNavigatorArgs) {
        delegate?.presented(source: Self.name, target: ThinkingDetailCollector.name)
        instance?.present(thinkingDetail: thinkingDetail)
    }

    public func present(attachmentPreview: AttachmentPreviewNavigatorArgs) {
        delegate?.presented(source: Self.name, target: AttachmentPreviewCollector.name)
        instance?.present(attachmentPreview: attachmentPreview)
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "conversation"
}

public class StuffOverviewCollector: StuffOverviewNavigator {
    private weak var instance: StuffOverviewNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: StuffOverviewNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func navigateTo(stuffDetail: StuffDetailNavigatorArgs) {
        delegate?.navigatedTo(source: Self.name, target: StuffDetailCollector.name)
        instance?.navigateTo(stuffDetail: stuffDetail)
    }

    public func present(appSettings: AppSettingsNavigatorArgs) {
        delegate?.presented(source: Self.name, target: AppSettingsCollector.name)
        instance?.present(appSettings: appSettings)
    }

    public func present(newConversation: NewConversationNavigatorArgs) {
        delegate?.presented(source: Self.name, target: NewConversationCollector.name)
        instance?.present(newConversation: newConversation)
    }

    public func present(agentBuilder: AgentBuilderNavigatorArgs) {
        delegate?.presented(source: Self.name, target: AgentBuilderCollector.name)
        instance?.present(agentBuilder: agentBuilder)
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "stuff_overview"
}

public class StuffDetailCollector: StuffDetailNavigator {
    private weak var instance: StuffDetailNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: StuffDetailNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "stuff_detail"
}

public class AppSettingsCollector: AppSettingsNavigator {
    private weak var instance: AppSettingsNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: AppSettingsNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func navigateTo(myInfo: MyInfoNavigatorArgs) {
        delegate?.navigatedTo(source: Self.name, target: MyInfoCollector.name)
        instance?.navigateTo(myInfo: myInfo)
    }

    public func navigateTo(customize: CustomizeSettingsNavigatorArgs) {
        delegate?.navigatedTo(source: Self.name, target: CustomizeSettingsCollector.name)
        instance?.navigateTo(customize: customize)
    }

    public func navigateTo(assistants: AssistantSettingsNavigatorArgs) {
        delegate?.navigatedTo(source: Self.name, target: AssistantSettingsCollector.name)
        instance?.navigateTo(assistants: assistants)
    }

    public func navigateTo(connections: ConnectionsNavigatorArgs) {
        delegate?.navigatedTo(source: Self.name, target: ConnectionsCollector.name)
        instance?.navigateTo(connections: connections)
    }

    public func navigateTo(backupRestore: BackupRestoreNavigatorArgs) {
        delegate?.navigatedTo(source: Self.name, target: BackupRestoreCollector.name)
        instance?.navigateTo(backupRestore: backupRestore)
    }

    public func navigateTo(deleteAllData: DeleteAllDataNavigatorArgs) {
        delegate?.navigatedTo(source: Self.name, target: DeleteAllDataCollector.name)
        instance?.navigateTo(deleteAllData: deleteAllData)
    }

    public func navigateTo(devices: DevicesNavigatorArgs) {
        delegate?.navigatedTo(source: Self.name, target: DevicesCollector.name)
        instance?.navigateTo(devices: devices)
    }

    public func present(paywall: PaywallNavigatorArgs) {
        delegate?.presented(source: Self.name, target: PaywallCollector.name)
        instance?.present(paywall: paywall)
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "app_settings"
}

public class NewConversationCollector: NewConversationNavigator {
    private weak var instance: NewConversationNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: NewConversationNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func navigateTo(conversation: ConversationNavigatorArgs) {
        delegate?.navigatedTo(source: Self.name, target: ConversationCollector.name)
        instance?.navigateTo(conversation: conversation)
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "new_conversation"
}

public class ExplodeConfirmationCollector: ExplodeConfirmationNavigator {
    private weak var instance: ExplodeConfirmationNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: ExplodeConfirmationNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "explode_confirmation"
}

public class ConversationInfoCollector: ConversationInfoNavigator {
    private weak var instance: ConversationInfoNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: ConversationInfoNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func navigateTo(edit: ConversationInfoEditNavigatorArgs) {
        delegate?.navigatedTo(source: Self.name, target: ConversationInfoEditCollector.name)
        instance?.navigateTo(edit: edit)
    }

    public func navigateTo(membersList: MembersListNavigatorArgs) {
        delegate?.navigatedTo(source: Self.name, target: MembersListCollector.name)
        instance?.navigateTo(membersList: membersList)
    }

    public func navigateTo(filesAndLinks: AgentFilesLinksNavigatorArgs) {
        delegate?.navigatedTo(source: Self.name, target: AgentFilesLinksCollector.name)
        instance?.navigateTo(filesAndLinks: filesAndLinks)
    }

    public func navigateTo(agentTemplateContactCard: AgentTemplateContactCardNavigatorArgs) {
        delegate?.navigatedTo(source: Self.name, target: AgentTemplateContactCardCollector.name)
        instance?.navigateTo(agentTemplateContactCard: agentTemplateContactCard)
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "conversation_info"
}

public class ConversationInfoEditCollector: ConversationInfoEditNavigator {
    private weak var instance: ConversationInfoEditNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: ConversationInfoEditNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "conversation_info_edit"
}

public class MembersListCollector: MembersListNavigator {
    private weak var instance: MembersListNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: MembersListNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func navigateTo(memberProfile: MemberProfileNavigatorArgs) {
        delegate?.navigatedTo(source: Self.name, target: MemberProfileCollector.name)
        instance?.navigateTo(memberProfile: memberProfile)
    }

    public func navigateTo(agentTemplateContactCard: AgentTemplateContactCardNavigatorArgs) {
        delegate?.navigatedTo(source: Self.name, target: AgentTemplateContactCardCollector.name)
        instance?.navigateTo(agentTemplateContactCard: agentTemplateContactCard)
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "members_list"
}

public class MemberProfileCollector: MemberProfileNavigator {
    private weak var instance: MemberProfileNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: MemberProfileNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "member_profile"
}

public class ShareInviteCollector: ShareInviteNavigator {
    private weak var instance: ShareInviteNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: ShareInviteNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "share_invite"
}

public class ReactionsCollector: ReactionsNavigator {
    private weak var instance: ReactionsNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: ReactionsNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "reactions"
}

public class AgentFilesLinksCollector: AgentFilesLinksNavigator {
    private weak var instance: AgentFilesLinksNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: AgentFilesLinksNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func present(attachmentPreview: AttachmentPreviewNavigatorArgs) {
        delegate?.presented(source: Self.name, target: AttachmentPreviewCollector.name)
        instance?.present(attachmentPreview: attachmentPreview)
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "agent_files_links"
}

public class SetupProfileCollector: SetupProfileNavigator {
    private weak var instance: SetupProfileNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: SetupProfileNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "setup_profile"
}

public class InviteAcceptedCollector: InviteAcceptedNavigator {
    private weak var instance: InviteAcceptedNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: InviteAcceptedNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "invite_accepted"
}

public class RequestPushNotificationsCollector: RequestPushNotificationsNavigator {
    private weak var instance: RequestPushNotificationsNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: RequestPushNotificationsNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "request_push_notifications"
}

public class MyInfoCollector: MyInfoNavigator {
    private weak var instance: MyInfoNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: MyInfoNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func navigateTo(quicknameRandomizer: QuicknameRandomizerNavigatorArgs) {
        delegate?.navigatedTo(source: Self.name, target: QuicknameRandomizerCollector.name)
        instance?.navigateTo(quicknameRandomizer: quicknameRandomizer)
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "my_info"
}

public class CustomizeSettingsCollector: CustomizeSettingsNavigator {
    private weak var instance: CustomizeSettingsNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: CustomizeSettingsNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "customize_settings"
}

public class AssistantSettingsCollector: AssistantSettingsNavigator {
    private weak var instance: AssistantSettingsNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: AssistantSettingsNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func present(inviteCodeEntry: InviteCodeEntryNavigatorArgs) {
        delegate?.presented(source: Self.name, target: InviteCodeEntryCollector.name)
        instance?.present(inviteCodeEntry: inviteCodeEntry)
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "assistant_settings"
}

public class ConnectionsCollector: ConnectionsNavigator {
    private weak var instance: ConnectionsNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: ConnectionsNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func present(connectionGrant: ConnectionGrantNavigatorArgs) {
        delegate?.presented(source: Self.name, target: ConnectionGrantCollector.name)
        instance?.present(connectionGrant: connectionGrant)
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "connections"
}

public class BackupRestoreCollector: BackupRestoreNavigator {
    private weak var instance: BackupRestoreNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: BackupRestoreNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "backup_restore"
}

public class DeleteAllDataCollector: DeleteAllDataNavigator {
    private weak var instance: DeleteAllDataNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: DeleteAllDataNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "delete_all_data"
}

public class QuicknameRandomizerCollector: QuicknameRandomizerNavigator {
    private weak var instance: QuicknameRandomizerNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: QuicknameRandomizerNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "quickname_randomizer"
}

public class InviteCodeEntryCollector: InviteCodeEntryNavigator {
    private weak var instance: InviteCodeEntryNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: InviteCodeEntryNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "invite_code_entry"
}

public class ConnectionGrantCollector: ConnectionGrantNavigator {
    private weak var instance: ConnectionGrantNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: ConnectionGrantNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "connection_grant"
}

public class DevicesCollector: DevicesNavigator {
    private weak var instance: DevicesNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: DevicesNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func present(pairDevice: PairDeviceNavigatorArgs) {
        delegate?.presented(source: Self.name, target: PairDeviceCollector.name)
        instance?.present(pairDevice: pairDevice)
    }

    public func present(removeDevice: RemoveDeviceNavigatorArgs) {
        delegate?.presented(source: Self.name, target: RemoveDeviceCollector.name)
        instance?.present(removeDevice: removeDevice)
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "devices"
}

public class PairDeviceCollector: PairDeviceNavigator {
    private weak var instance: PairDeviceNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: PairDeviceNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "pair_device"
}

public class RemoveDeviceCollector: RemoveDeviceNavigator {
    private weak var instance: RemoveDeviceNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: RemoveDeviceNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "remove_device"
}

public class ExplodeInfoCollector: ExplodeInfoNavigator {
    private weak var instance: ExplodeInfoNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: ExplodeInfoNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "explode_info"
}

public class PinLimitInfoCollector: PinLimitInfoNavigator {
    private weak var instance: PinLimitInfoNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: PinLimitInfoNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "pin_limit_info"
}

public class LockedConvoInfoCollector: LockedConvoInfoNavigator {
    private weak var instance: LockedConvoInfoNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: LockedConvoInfoNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func present(lockConfirmation: LockConvoConfirmationNavigatorArgs) {
        delegate?.presented(source: Self.name, target: LockConvoConfirmationCollector.name)
        instance?.present(lockConfirmation: lockConfirmation)
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "locked_convo_info"
}

public class FullConvoInfoCollector: FullConvoInfoNavigator {
    private weak var instance: FullConvoInfoNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: FullConvoInfoNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "full_convo_info"
}

public class ConversationForkedInfoCollector: ConversationForkedInfoNavigator {
    private weak var instance: ConversationForkedInfoNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: ConversationForkedInfoNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "conversation_forked_info"
}

public class RevealMediaInfoCollector: RevealMediaInfoNavigator {
    private weak var instance: RevealMediaInfoNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: RevealMediaInfoNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "reveal_media_info"
}

public class PhotosInfoCollector: PhotosInfoNavigator {
    private weak var instance: PhotosInfoNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: PhotosInfoNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "photos_info"
}

public class AssistantConfirmationCollector: AssistantConfirmationNavigator {
    private weak var instance: AssistantConfirmationNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: AssistantConfirmationNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "assistant_confirmation"
}

public class AgentInfoCollector: AgentInfoNavigator {
    private weak var instance: AgentInfoNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: AgentInfoNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "agent_info"
}

public class AgentPowerInfoCollector: AgentPowerInfoNavigator {
    private weak var instance: AgentPowerInfoNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: AgentPowerInfoNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "agent_power_info"
}

public class ExplodedInviteInfoCollector: ExplodedInviteInfoNavigator {
    private weak var instance: ExplodedInviteInfoNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: ExplodedInviteInfoNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "exploded_invite_info"
}

public class BackwardsSecrecyInfoCollector: BackwardsSecrecyInfoNavigator {
    private weak var instance: BackwardsSecrecyInfoNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: BackwardsSecrecyInfoNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "backwards_secrecy_info"
}

public class LockConvoConfirmationCollector: LockConvoConfirmationNavigator {
    private weak var instance: LockConvoConfirmationNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: LockConvoConfirmationNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "lock_convo_confirmation"
}

public class ContactsCollector: ContactsNavigator {
    private weak var instance: ContactsNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: ContactsNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func navigateTo(contactCard: ContactCardNavigatorArgs) {
        delegate?.navigatedTo(source: Self.name, target: ContactCardCollector.name)
        instance?.navigateTo(contactCard: contactCard)
    }

    public func present(newConversation: NewConversationNavigatorArgs) {
        delegate?.presented(source: Self.name, target: NewConversationCollector.name)
        instance?.present(newConversation: newConversation)
    }

    public func present(appSettings: AppSettingsNavigatorArgs) {
        delegate?.presented(source: Self.name, target: AppSettingsCollector.name)
        instance?.present(appSettings: appSettings)
    }

    public func present(agentBuilder: AgentBuilderNavigatorArgs) {
        delegate?.presented(source: Self.name, target: AgentBuilderCollector.name)
        instance?.present(agentBuilder: agentBuilder)
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "contacts"
}

public class ContactCardCollector: ContactCardNavigator {
    private weak var instance: ContactCardNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: ContactCardNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func navigateTo(contacts: ContactsNavigatorArgs) {
        delegate?.navigatedTo(source: Self.name, target: ContactsCollector.name)
        instance?.navigateTo(contacts: contacts)
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "contact_card"
}

public class AgentTemplateContactCardCollector: AgentTemplateContactCardNavigator {
    private weak var instance: AgentTemplateContactCardNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: AgentTemplateContactCardNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "agent_template_contact_card"
}

public class AddMembersCollector: AddMembersNavigator {
    private weak var instance: AddMembersNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: AddMembersNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "add_members"
}

public class AgentBuilderCollector: AgentBuilderNavigator {
    private weak var instance: AgentBuilderNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: AgentBuilderNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "agent_builder"
}

public class ThinkingDetailCollector: ThinkingDetailNavigator {
    private weak var instance: ThinkingDetailNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: ThinkingDetailNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "thinking_detail"
}

public class AttachmentPreviewCollector: AttachmentPreviewNavigator {
    private weak var instance: AttachmentPreviewNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: AttachmentPreviewNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func navigateTo(contactCard: ContactCardNavigatorArgs) {
        delegate?.navigatedTo(source: Self.name, target: ContactCardCollector.name)
        instance?.navigateTo(contactCard: contactCard)
    }

    public func navigateTo(agentTemplateContactCard: AgentTemplateContactCardNavigatorArgs) {
        delegate?.navigatedTo(source: Self.name, target: AgentTemplateContactCardCollector.name)
        instance?.navigateTo(agentTemplateContactCard: agentTemplateContactCard)
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "attachment_preview"
}

public class PaywallCollector: PaywallNavigator {
    private weak var instance: PaywallNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: PaywallNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "paywall"
}

public class SubscriptionSettingsCollector: SubscriptionSettingsNavigator {
    private weak var instance: SubscriptionSettingsNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: SubscriptionSettingsNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func present(paywall: PaywallNavigatorArgs) {
        delegate?.presented(source: Self.name, target: PaywallCollector.name)
        instance?.present(paywall: paywall)
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "subscription_settings"
}

public class BillingDebugCollector: BillingDebugNavigator {
    private weak var instance: BillingDebugNavigator?
    private weak var delegate: CollectorDelegate?

    public init(instance: BillingDebugNavigator, delegate: CollectorDelegate) {
        self.instance = instance
        self.delegate = delegate
    }

    public func present(paywall: PaywallNavigatorArgs) {
        delegate?.presented(source: Self.name, target: PaywallCollector.name)
        instance?.present(paywall: paywall)
    }

    public func navigateTo(subscriptionSettings: SubscriptionSettingsNavigatorArgs) {
        delegate?.navigatedTo(source: Self.name, target: SubscriptionSettingsCollector.name)
        instance?.navigateTo(subscriptionSettings: subscriptionSettings)
    }

    public func closed(context: ScreenContext) {
        delegate?.closed(screen: Self.name, context: context)
        instance?.closed(context: context)
    }

    public static let name: String = "billing_debug"
}
