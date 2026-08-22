# Navigator / Core Action audit — Android drift

> **Status: applied.** The descriptors (`NavigationTargets.kt`,
> `CoreActions.kt`) were updated per the decisions below and the build
> regenerated `README.md`, `navigators.dot`, `navigators.png`, and the
> `ConvosMetrics` Swift package. Excluded by request: `qr_scanner`
> (QRScannerScreen is being removed) and all debug screens.



Compares the canonical catalog in this repo (`NavigationTargets.kt`,
`CoreActions.kt`, rendered in `README.md` / `navigators.dot`) against the
actual Android client at `../convos-client/android/`.

Verified directly against Android `*Activity.kt`, `*ViewModel.kt`,
`*Screen.kt`, and `metrics.actions.*` call sites (build output excluded).

## Summary

| Bucket | Count | Items |
|--------|-------|-------|
| Catalog entries to **remove** | 2 | `maxed_out_info` navigator, `low_balance_banner_shown` core action |
| Navigators **missing** from catalog (real Android screens) | 9 | qr_scanner, add_members, contacts, contact_card, agent_template_contact_card, agent_builder, thinking_detail, html_attachment_preview, developer_settings (+3 debug) |
| Transitions **missing** from catalog | 5+ | see transitions table |
| Modeling / signature drift | 1 | scanner modeled as a `new_conversation` mode but is its own screen |

## 1. Remove from catalog

| Type | Catalog entry | Reason |
|------|---------------|--------|
| Navigator | `maxed_out_info` | Orphan node in `navigators.dot` (declared, **no incoming or outgoing edges**) and **zero references** anywhere in Android (`grep -i maxedout\|maxed_out` → none). Dead entry. |
| Core action | `low_balance_banner_shown` | **Not emitted anywhere** in Android (`grep lowBalanceBannerShown\|low_balance_banner` → none). The only canonical core action with no Android call site. Either wire it on Android or drop it from the shared catalog. |

## 2. Missing from catalog — real Android navigators

Each has a backing `Activity` and/or `ViewModel`/`Screen`, so they are genuine
navigation destinations the catalog does not model.

Added (✅) or excluded by request (⛔).

| Name | Android source | Purpose | Args | Status |
|------|----------------|---------|------|--------|
| `add_members` | `AddMembersActivity` + `ContactsPickerViewModel` | Contact picker to add members to a conversation | `conversationId`, `conversationTitle?` | ✅ present from `conversation` |
| `contacts` | `ContactsActivity` + `ContactsListViewModel` | Contacts / people list | _none_ | ✅ navigateTo from `app_settings` (+ from `contact_card`) |
| `contact_card` | `ContactCardActivity` + `ContactCardViewModel` | Standalone contact detail card (distinct from convo-scoped `member_profile`) | `inboxId`, `conversationId?` | ✅ from `conversations`, `conversation`, `contacts`, `html_attachment_preview` |
| `agent_template_contact_card` | `AgentTemplateContactCardActivity` + VM | Agent-template card (deep-link / in-chat agent tap), `isAgentTemplatesEnabled`-gated | `templateId`, `inboxId`, `conversationId?` | ✅ from `conversation`, `conversation_info`, `members_list`, `html_attachment_preview` |
| `agent_builder` | `AgentBuilderScreen` + `AgentBuilderViewModel` | Create / customize an agent | `conversationId` | ✅ present from `conversations`, `conversation` |
| `thinking_detail` | `ThinkingDetailSheet` + `ThinkingDetailViewModel` | Agent reasoning / "thinking" sheet | `conversationId`, `senderInboxId`, `messageId` | ✅ present from `conversation` |
| `html_attachment_preview` | `HtmlAttachmentPreviewActivity` | Rich HTML attachment preview | `conversationId?`, `senderInboxId?` | ✅ present from `conversation`, `assistant_files_links` |
| `qr_scanner` | `QRScannerActivity` + VM + Screen | Camera invite-code scanner | — | ⛔ excluded — QRScannerScreen being removed |
| `developer_settings`, `meters_debug`, `backend_auth_probe`, `debug_message_list` | various `*Activity` | Debug surfaces | — | ⛔ excluded — debug screens out of scope |

`ConnectionsCallbackActivity` is an OAuth-redirect target, not a user-facing
navigator — left out.

## 3. Confirmed present (catalog ↔ Android match)

All 10 remaining core actions are emitted with matching signatures:
`started_conversation`, `joined_conversation` (full args incl. `source`),
`invited_to_conversation`, `added_assistant`, `sent_message`,
`purchase_initiated`, `purchase_succeeded`, `purchase_cancelled`,
`purchase_failed`, `purchases_restored`.

Catalog navigators with confirmed Android counterparts (generated `*Collector`
wrappers consumed by the app): conversations, conversation, app_settings,
new_conversation, explode_confirmation, conversation_info,
conversation_info_edit, members_list, member_profile, share_invite (metrics
marker + system share sheet), reactions, assistant_files_links, setup_profile,
invite_accepted, request_push_notifications, my_info, customize_settings,
assistant_settings, connections, quickname_randomizer, connection_grant,
paywall, subscription_settings, billing_debug, and the info sheets
(explode_info, pin_limit_info, locked_convo_info, full_convo_info,
conversation_forked_info, reveal_media_info, photos_info,
assistant_confirmation, assistant_info, processing_power_info,
exploded_invite_info, backwards_secrecy_info, lock_convo_confirmation).

## 4. Missing transitions / modeling drift

| From (catalog) | To | Status |
|----------------|----|--------|
| `conversation` | `add_members` | ✅ wired (launched from `ConversationActivity` `onAddFromContactsTap`). |
| `conversation` | `thinking_detail` | ✅ wired. |
| `conversation` / `members_list` / `conversation_info` | `contact_card` / `agent_template_contact_card` | ✅ wired (member taps use `agent_template_contact_card` when scoped; `contact_card` for standalone). |
| `assistant_files_links` | `html_attachment_preview` | ✅ wired (`OpenAssistantFile`). |
| `new_conversation` (mode `SCANNER`) | `qr_scanner` | Resolved — scanner is being removed, so no navigator added. `NewConversationMode.SCANNER` left untouched pending separate decision. |

## Outcome

1. ✅ Removed `maxed_out_info` navigator and `low_balance_banner_shown`
   core action from the descriptors.
2. ✅ Added the seven non-debug Android navigators in §2 and wired their
   edges (§4).
3. ✅ Excluded `qr_scanner` (screen being removed) and all debug screens.
4. ✅ Ran `metrics/gradlew build` — regenerated README catalog,
   `navigators.dot`, `navigators.png`, and the `ConvosMetrics` Swift package.
