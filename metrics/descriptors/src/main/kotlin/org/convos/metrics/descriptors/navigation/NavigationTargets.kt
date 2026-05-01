package org.convos.metrics.descriptors.navigation

import org.convos.metrics.annotations.NavigationTarget

// Primary Screens

@NavigationTarget
interface ConversationsScreen {
    class Args

    fun navigateTo(conversation: ConversationScreen.Args)
    fun present(appSettings: AppSettingsScreen.Args)
    fun present(newConversation: NewConversationScreen.Args)
    fun present(explodeConfirmation: ExplodeConfirmationScreen.Args)
    fun present(connectionGrant: ConnectionGrantScreen.Args)
    fun present(explodeInfo: ExplodeInfoScreen.Args)
    fun present(pinLimitInfo: PinLimitInfoScreen.Args)

    fun closed()
}

@NavigationTarget
interface ConversationScreen {
    data class Args(val conversationId: String)

    fun present(conversationInfo: ConversationInfoScreen.Args)
    fun present(myInfo: MyInfoScreen.Args)
    fun present(memberProfile: MemberProfileScreen.Args)
    fun present(shareInvite: ShareInviteScreen.Args)
    fun present(newConversation: NewConversationScreen.Args)
    fun present(reactions: ReactionsScreen.Args)
    fun present(explodeInfo: ExplodeInfoScreen.Args)
    fun present(lockedConvoInfo: LockedConvoInfoScreen.Args)
    fun present(fullConvoInfo: FullConvoInfoScreen.Args)
    fun present(conversationForkedInfo: ConversationForkedInfoScreen.Args)
    fun present(revealMediaInfo: RevealMediaInfoScreen.Args)
    fun present(photosInfo: PhotosInfoScreen.Args)
    fun present(assistantConfirmation: AssistantConfirmationScreen.Args)
    fun present(assistantInfo: AssistantInfoScreen.Args)
    fun present(processingPowerInfo: ProcessingPowerInfoScreen.Args)
    fun present(explodedInviteInfo: ExplodedInviteInfoScreen.Args)

    fun closed()
}

// Root-Level Modals

@NavigationTarget
interface AppSettingsScreen {
    class Args

    fun navigateTo(myInfo: MyInfoScreen.Args)
    fun navigateTo(customize: CustomizeSettingsScreen.Args)
    fun navigateTo(assistants: AssistantSettingsScreen.Args)
    fun navigateTo(connections: ConnectionsScreen.Args)
    fun navigateTo(backupRestore: BackupRestoreScreen.Args)
    fun navigateTo(deleteAllData: DeleteAllDataScreen.Args)

    fun closed()
}

@NavigationTarget
interface NewConversationScreen {
    data class Args(
        val mode: NewConversationMode,
        val inviteCode: String? = null,
    )

    fun closed()
}

enum class NewConversationMode {
    CREATE,
    SCANNER,
    JOIN_INVITE,
}

@NavigationTarget
interface ExplodeConfirmationScreen {
    data class Args(val conversationId: String)

    fun closed()
}

// Conversation Detail Modals

@NavigationTarget
interface ConversationInfoScreen {
    data class Args(val conversationId: String)

    fun navigateTo(edit: ConversationInfoEditScreen.Args)
    fun navigateTo(membersList: MembersListScreen.Args)

    fun closed()
}

@NavigationTarget
interface ConversationInfoEditScreen {
    data class Args(val conversationId: String)

    fun closed()
}

@NavigationTarget
interface MembersListScreen {
    data class Args(val conversationId: String)

    fun navigateTo(memberProfile: MemberProfileScreen.Args)

    fun closed()
}

@NavigationTarget
interface MemberProfileScreen {
    data class Args(
        val conversationId: String,
        val memberId: String,
    )

    fun closed()
}

@NavigationTarget
interface ShareInviteScreen {
    data class Args(val conversationId: String)

    fun closed()
}

@NavigationTarget
interface ReactionsScreen {
    data class Args(
        val conversationId: String,
        val messageId: String,
    )

    fun closed()
}

// Settings Sub-Screens

@NavigationTarget
interface MyInfoScreen {
    class Args

    fun navigateTo(quicknameRandomizer: QuicknameRandomizerScreen.Args)

    fun closed()
}

@NavigationTarget
interface CustomizeSettingsScreen {
    class Args

    fun closed()
}

@NavigationTarget
interface AssistantSettingsScreen {
    class Args

    fun present(inviteCodeEntry: InviteCodeEntryScreen.Args)

    fun closed()
}

@NavigationTarget
interface ConnectionsScreen {
    class Args

    fun present(connectionGrant: ConnectionGrantScreen.Args)

    fun closed()
}

@NavigationTarget
interface BackupRestoreScreen {
    class Args

    fun closed()
}

@NavigationTarget
interface DeleteAllDataScreen {
    class Args

    fun closed()
}

@NavigationTarget
interface QuicknameRandomizerScreen {
    class Args

    fun closed()
}

@NavigationTarget
interface InviteCodeEntryScreen {
    class Args

    fun closed()
}

@NavigationTarget
interface ConnectionGrantScreen {
    data class Args(
        val serviceId: String,
        val conversationId: String,
    )

    fun closed()
}

// Info Sheets

@NavigationTarget
interface ExplodeInfoScreen {
    class Args

    fun closed()
}

@NavigationTarget
interface PinLimitInfoScreen {
    class Args

    fun closed()
}

@NavigationTarget
interface LockedConvoInfoScreen {
    data class Args(val conversationId: String)

    fun closed()
}

@NavigationTarget
interface FullConvoInfoScreen {
    class Args

    fun closed()
}

@NavigationTarget
interface ConversationForkedInfoScreen {
    data class Args(val conversationId: String)

    fun closed()
}

@NavigationTarget
interface RevealMediaInfoScreen {
    class Args

    fun closed()
}

@NavigationTarget
interface PhotosInfoScreen {
    class Args

    fun closed()
}

@NavigationTarget
interface AssistantConfirmationScreen {
    data class Args(val conversationId: String)

    fun closed()
}

@NavigationTarget
interface AssistantInfoScreen {
    class Args

    fun closed()
}

@NavigationTarget
interface ProcessingPowerInfoScreen {
    class Args

    fun closed()
}

@NavigationTarget
interface ExplodedInviteInfoScreen {
    class Args

    fun closed()
}
