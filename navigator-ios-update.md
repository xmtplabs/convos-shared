# Navigator catalog update -- iOS drift

> **Status: proposed.** This document is the iOS counterpart to
> `navigator-android-update.md` (applied 2026-05-29). It is a *plan*, not yet
> applied. Apply by editing
> `metrics/descriptors/src/main/kotlin/org/convos/metrics/descriptors/navigation/NavigationTargets.kt`,
> then running `cd metrics && ./gradlew build` to regenerate `README.md`,
> `navigators.dot`, `navigators.png`, and the `ConvosMetrics` Swift package.

Compares the canonical catalog in this repo (`NavigationTargets.kt`,
rendered in `README.md` / `navigators.dot`) against the actual iOS client at
`../convos-ios/` (branch `dev`, HEAD `5db90da7`, 2026-06-03). The full audit
that backs this plan is `../convos-ios/production-nav.md`.

Verified directly against iOS view files, view models, and modifier call
sites under `Convos/`, `ConvosCore/`, and `ConvosAppClip/`.

## Summary

| Bucket | Count | Items |
|--------|-------|-------|
| Navigators **missing** from catalog (real iOS screens) | 6 | `stuff_overview`, `stuff_detail`, `devices`, `pair_device`, `remove_device`, `tab_root` (modeling) |
| Edges **missing** from catalog | 9 | see §3 |
| Edges to **remove** from catalog | 2 | `app_settings -> contacts`, `app_settings -> subscription_settings` (iOS routes direct to `paywall`) |
| Catalog entries with **renames** to consider | 7 | `assistant_*` (4) + `processing_power_info` + `html_attachment_preview` + `qr_scanner` re-add |
| Catalog entries to **verify still needed** (no iOS impl; Android-only) | 7 | `assistant_confirmation`, `assistant_settings`, `quickname_randomizer`, `invite_code_entry`, `backup_restore`, `exploded_invite_info`, `billing_debug` |
| Modeling drift | 3 | tab shell unmodeled; `app_settings` presentation re-parenting; `ContactDetailView` consolidates 3 catalog nodes |

The catalog is **cross-platform**. Every removal or rename listed below
must be confirmed against the Android client at
`../convos-client/android/` before being applied. The May 2026
android-update audit confirmed Android still references most of the
"verify" bucket above -- those entries are likely Android-only and should
stay until Android also drops them.

## 1. Add to catalog -- real iOS navigators

Each has a backing SwiftUI `View` and an entry point exercised in
production, so they are genuine navigation destinations the catalog does
not yet model.

| Name | iOS source | Purpose | Args | Suggested wiring |
|------|------------|---------|------|------------------|
| `stuff_overview` | `Convos/Conversations List/StuffTabView.swift` | Tab root: grid of agent-generated content (HTML attachments) across all conversations | _none_ | tab root (see §5 for modeling); push -> `stuff_detail` |
| `stuff_detail` | `Convos/Conversations List/StuffDetailView.swift` | Push destination from `stuff_overview`; hydrates a conversation view model used by the floating overlay | `itemId`, `conversationId?` | leaf (the overlay morph is not a navigation event in catalog terms) |
| `devices` | `Convos/Devices/DevicesView.swift` | List paired devices, manage pairing | _none_ | navigateTo from `app_settings`; present -> `pair_device`, `remove_device` |
| `pair_device` | `Convos/Devices/PairingSheetView.swift` | Initiate device pairing sheet | `pairingId?`, `initiatorName?`, `expiresAt?` | present from `devices`; also landing target for `DeepLinkDestination.pairDevice(...)` |
| `remove_device` | `Convos/Devices/RemoveDeviceSheetView.swift` | Confirm + execute removal of a paired device | `deviceId` | present from `devices` |
| `tab_root` (modeling pseudo-node) | `Convos/MainTabView.swift` | Hosts the three tabs and the floating overlay; presents `app_settings`, `agent_builder`, `new_conversation`, `compose_flow`, `camera_picker`, `photo_picker` | _none_ | see §5 -- not necessarily a navigator interface; may be modeled as a parent of the three tab roots instead |

**Suggested Kotlin additions** (in
`NavigationTargets.kt`):

```kotlin
@NavigationTarget
interface StuffOverviewNavigator {
    class Args
    fun navigateTo(stuffDetail: StuffDetailNavigator.Args)
    fun closed(context: ScreenContext)
}

@NavigationTarget
interface StuffDetailNavigator {
    data class Args(
        val itemId: String,
        val conversationId: String? = null,
    )
    fun closed(context: ScreenContext)
}

@NavigationTarget
interface DevicesNavigator {
    class Args
    fun present(pairDevice: PairDeviceNavigator.Args)
    fun present(removeDevice: RemoveDeviceNavigator.Args)
    fun closed(context: ScreenContext)
}

@NavigationTarget
interface PairDeviceNavigator {
    data class Args(
        val pairingId: String? = null,
        val initiatorName: String? = null,
        val expiresAt: Long? = null,
    )
    fun closed(context: ScreenContext)
}

@NavigationTarget
interface RemoveDeviceNavigator {
    data class Args(val deviceId: String)
    fun closed(context: ScreenContext)
}
```

Wire `app_settings -> devices` by adding:

```kotlin
// inside AppSettingsNavigator
fun navigateTo(devices: DevicesNavigator.Args)
```

## 2. Re-add to catalog -- excluded by Android audit but still on iOS

| Name | iOS source | Reason to re-add |
|------|------------|------------------|
| `qr_scanner` | `Convos/Conversation Creation/QRScannerView.swift` | The Android audit excluded `qr_scanner` because Android was removing the screen. iOS still ships it from the compose flow. Either re-add (with parameter modeling for iOS) or remove from iOS to match Android's direction. Pick one path before applying. |

Suggested Kotlin if re-adding:

```kotlin
@NavigationTarget
interface QrScannerNavigator {
    class Args
    fun navigateTo(conversation: ConversationNavigator.Args)
    fun closed(context: ScreenContext)
}
```

Source edges to consider: presented from `new_conversation` (the
`SCANNER` mode currently has no destination link in the catalog), or
from `tab_root` if compose-from-shell is the production entry.

**Decision required before applying.** If we keep `NewConversationMode`
with a `SCANNER` value but the scanner is its own screen, the catalog
already disagrees with itself (a mode value with no corresponding
navigator edge).

## 3. Add missing transitions

| From (catalog) | To (catalog) | iOS source | Type |
|----------------|--------------|------------|------|
| `app_settings` | `devices` | `AppSettingsView.swift:130` | `navigateTo` |
| `devices` | `pair_device` | `DevicesView` `showPairingSheet` | `present` |
| `devices` | `remove_device` | `DevicesView` per-device remove | `present` |
| `app_settings` | `paywall` | `AppSettingsView.swift:232,237` "Power" row | `present` |
| `conversation` | `paywall` (already present) -- verify `source` arg matches `PaywallSource.LOW_BALANCE_BANNER` / `MEMBER_CARD` | `ConversationView.swift:142,423`; `ConversationMemberView.swift:36,92` | confirm `Args(source:)` |
| `stuff_overview` | `stuff_detail` | `StuffTabView.swift:65` | `navigateTo` |
| `stuff_detail` | `conversation` (hydration via overlay) | `StuffDetailView` `syncPushedConvoVM()` | not strictly navigation -- document as such |
| `qr_scanner` | `conversation` (if re-added) | `QRScannerView` callback | `navigateTo` |
| `tab_root` | `app_settings` | `MainTabView.swift:483-549,825` `AppIndicatorPill` | `present` -- replaces `conversations -> app_settings` (see §4) |

## 4. Remove from catalog -- edges that do not exist on iOS

| Edge | Catalog source | iOS reality |
|------|----------------|-------------|
| `app_settings -> contacts` (navigators.dot:97) | `AppSettingsNavigator.navigateTo(contacts: ContactsNavigator.Args)` | Contacts is a *tab*, not a settings child. Verify Android also does not push to contacts from settings; if Android does, mark this edge Android-only or split the catalog. |
| `app_settings -> subscription_settings` (navigators.dot:96) and the implicit `subscription_settings -> paywall` chain | `AppSettingsNavigator.navigateTo(subscriptionSettings: ...)` | iOS settings "Power" row goes *direct* to `paywall`. `SubscriptionSettingsView` exists on iOS but is only reachable from `DebugView` (i.e. dev/local builds). Decide: (a) reflect iOS by replacing with `app_settings -> paywall`; (b) keep both, with `subscription_settings` flagged Android-only; (c) ship `SubscriptionSettingsView` in production iOS to match catalog. |
| `conversations -> app_settings` (navigators.dot:55) | `ConversationsNavigator.present(appSettings: ...)` | App Settings on iOS is presented from the shell-level `AppIndicatorPill` overlay, not from the Conversations screen. The pill is visible across all three tabs. Re-source this edge from a `tab_root` / shell node (see §5). |

## 5. Modeling decisions

These are not Kotlin edits per se -- they are design decisions the
maintainer needs to make before §1-§4 can be cleanly applied.

### 5.1 Tab shell

The catalog has no concept of "tab" or a parent shell. iOS has three
sibling tab roots (`conversations`, `stuff_overview`, `contacts`)
hanging off `MainTabView`, which itself owns shell-level sheets
(`app_settings`, `agent_builder`, `new_conversation`, `compose_flow`,
`camera_picker`, `photo_picker`).

Three options:

1. **Introduce a `tab_root` navigator.** Parent of the three tab navs;
   owner of all shell-level presentations. Most accurate, requires a
   new node and re-routing 6+ edges. Android likely uses a similar
   shell -- needs verification.
2. **Promote each tab root to present shell modals.** Add the same
   `present(appSettings: ...)` etc. methods to `ConversationsNavigator`,
   `StuffOverviewNavigator`, `ContactsNavigator`. Mirrors the current
   `conversations -> app_settings` modeling but spreads it across three
   navs. Easier; loses the "shell, not tab" distinction.
3. **Keep `conversations -> app_settings` and accept the
   approximation.** Simplest. Justifies it on the grounds that
   Conversations is the primary tab and the pill is most often tapped
   from there. Document the inaccuracy.

Recommendation: option 1 if Android also has a shell concept;
option 2 otherwise.

### 5.2 `ContactDetailView` consolidation

The catalog has three nodes that all map to one iOS view:

- `member_profile` <- `ContactDetailView(mode: .scopedToConversation)`
- `contact_card` <- `ContactDetailView(mode: .standalone)`
- `agent_template_contact_card` <- `ContactDetailView` when
  `contact.agentTemplateId != nil` (renders extra sections; the mode
  enum has no `.agentTemplate` case)

Two options:

1. **Keep the three catalog nodes.** Collector wrappers on iOS
   discriminate by `ContactDetailMode` plus `contact.agentTemplateId`
   and emit the correct metrics identifier. Preserves analytics
   granularity. Requires extra wiring in the Swift generator output
   (or a hand-written adapter at the iOS call sites).
2. **Collapse to one catalog node** with an `entryMode` argument enum.
   Cleaner code, but loses entry-point granularity in analytics
   dashboards that already key off the three names. Breaking change.

Recommendation: option 1. Keep the three nodes, document that iOS
implements them via one view + mode + data, and add a "how to instrument"
note to the generated Swift collectors.

### 5.3 Inline vs presented agent builder

The catalog has `conversations -> agent_builder` as a `present` edge.
iOS renders `AgentBuilderView(mode: .inline)` *inline* in the
Conversations empty state -- not as a sheet. After inline commit, the
shell sheet-presents the *resulting conversation* (a different edge).

Two options:

1. **Add a second edge** `conversations -> conversation` (already
   exists as `navigateTo`) marked as "after inline-builder commit".
   Treat the inline builder as an embedded view, not a navigation.
2. **Add a sub-mode** to `AgentBuilderNavigator.Args` (e.g.
   `EntryMode { INLINE, SHEET }`) so collector emissions can
   discriminate. Aligns with `NewConversationMode` precedent.

Recommendation: option 2.

### 5.4 Inline sheets vs dedicated navigators

`share_invite` and `explode_confirmation` are catalog navigators with
`Args`, but iOS implements both as inline sheet booleans inside
`ConversationInfoView` (no dedicated view file). The collector layer
has nothing concrete to wrap.

Two options:

1. **Extract `ShareInviteView` and `ExplodeConfirmationView`** in iOS
   so the collectors have real views to wrap. Cleanest. Requires an
   iOS PR.
2. **Document the inline pattern and emit the metrics manually** at the
   sheet's `isPresented` setter site. Avoids an iOS refactor; loses
   the "one wrapper per navigator" symmetry.

Recommendation: option 1, paired with the catalog updates.

## 6. Renames to consider (require Android confirmation)

The Android audit did not rename any of the `assistant_*` nodes, so
Android likely still uses "assistant" terminology in code. iOS has
moved to "agent". Two options for each:

| Catalog name | iOS name | Recommendation |
|---|---|---|
| `assistant_confirmation` | (no iOS impl) | Defer until iOS implements an equivalent agent-confirmation screen. |
| `assistant_info` | `AgentsInfoView` | Rename to `agent_info` if Android also uses "agent" terminology; otherwise keep and add iOS mapping in collectors. |
| `assistant_settings` | (no iOS impl) | See §7. |
| `assistant_files_links` | `AgentFilesLinksView` | Rename to `agent_files_links`. |
| `processing_power_info` | `AgentPowerInfoView` | Rename to `agent_power_info`. |
| `html_attachment_preview` | `AttachmentPreviewSheet` (generalized to all attachment types) | Rename to `attachment_preview` and broaden the doc string. |
| `qr_scanner` | `QRScannerView` | Re-add (see §2). |

Any rename is a breaking change to the generated Swift package -- iOS
collector call sites must be updated in the same PR cycle. The build
pre-commit hook will catch any mismatch between the staged Kotlin and
the regenerated Swift.

## 7. Verify still needed (Android-only candidates)

Per the May 2026 Android audit (`navigator-android-update.md` §3),
Android *does* reference these screens in production. They have no
iOS implementation. Keep them in the catalog; mark them as
Android-only if/when the catalog grows a `platforms` annotation. Until
then, **do not remove**.

- `assistant_confirmation` -- Android `ConversationActivity`
- `assistant_settings` -- Android settings child
- `quickname_randomizer` -- Android `MyInfo` child
- `invite_code_entry` -- Android `AssistantSettings` child
- `backup_restore` -- Android settings child (Android-confirmed
  implicitly in audit's "confirmed present" list)
- `exploded_invite_info` -- Android info sheet
- `billing_debug` -- Android debug screen

**Decision required:** does iOS want to add these screens to reach
parity, or leave them Android-only? This plan is a no-op for that
bucket -- it just flags the gap.

## 8. Apply procedure

When the decisions in §5-§7 are resolved:

1. Edit
   `metrics/descriptors/src/main/kotlin/org/convos/metrics/descriptors/navigation/NavigationTargets.kt`
   with the additions, removals, and renames decided above.
2. Run `cd metrics && ./gradlew build`. This:
   - regenerates the Swift package at
     `ConvosMetrics/Sources/ConvosMetrics/NavigationTargets.swift` +
     `NavigationCollectors.swift`,
   - regenerates `navigators.dot` (and `navigators.png` if Graphviz is
     installed),
   - splices the catalog into `README.md` between the AUTOGEN markers.
3. Update iOS in a paired PR on `convos-ios` to:
   - extract `ShareInviteView`, `ExplodeConfirmationView` (per §5.4),
   - wire collectors for the new nodes (Stuff, Devices),
   - update any renamed collector call sites,
   - bump the `ConvosMetrics` SPM revision pin.
4. Update Android in a paired PR on `convos-client` to mirror the same
   changes (collector call sites for new nodes if Android adopts them;
   renames; edge updates).
5. The `convos-shared` pre-commit hook (`./scripts/install-hooks.sh`)
   will re-run `./gradlew build` and abort if the regenerated outputs
   differ from staged. Verify the staged set is consistent before the
   commit.
6. Update the status block at the top of this file to "applied" and
   add the build commit SHA to match the format of
   `navigator-android-update.md`.

## 9. Out of scope

- App Clip (`AppClipRootView`) -- separate target with a single screen
  that funnels to install/open. No instrumentation needed.
- Debug screens (`DebugView` and children, `DebugAuthProbeView`,
  `DebugExportView`, `PendingInviteDebugView`,
  `OrphanedInboxDebugView`, `DebugAssetRenewalView`) -- excluded by the
  Android audit; same exclusion applies here.
- Deep-link landings as standalone catalog nodes -- the existing nodes
  already cover the landing surfaces (`conversation`, `devices`,
  `agent_builder`, etc.). Deep links are entry sources, not screens.
- Onboarding coordinator states (`.idle`, `.started`,
  `.settingUpProfile`, `.presentingPaywall`, ...) -- the three
  user-visible sheets (`setup_profile`, `invite_accepted`,
  `request_push_notifications`) already exist in the catalog; the
  coordinator's internal transitions do not warrant separate nodes.

## 10. Open questions before applying

1. Does Android have a shell concept analogous to iOS's `MainTabView`,
   or does it model each tab independently? Determines §5.1 choice.
2. Does Android still ship the seven "Android-only candidate" screens
   in §7, or has Android also retired some? Determines whether removals
   are safe.
3. Is the iOS Power-row -> Paywall direct route a bug or by design?
   If by design, the catalog should follow (§4). If a bug, file an iOS
   issue and leave the catalog edge intact.
4. Should the agent_template surface stay as its own catalog node
   (`agent_template_contact_card`) or collapse into `contact_card` with
   a template flag? Determines §5.2 choice.
5. Is the inline-builder empty-state pattern an iOS-only product
   choice, or does Android also have an inline builder? Determines
   §5.3 modeling.

------------------------------------------------------------------------

## Appendix A -- minimal patch summary

If all decisions favor the "match iOS today" path:

**Add (Kotlin interfaces):** `StuffOverviewNavigator`,
`StuffDetailNavigator`, `DevicesNavigator`, `PairDeviceNavigator`,
`RemoveDeviceNavigator`, (optional) `QrScannerNavigator`.

**Add methods on existing interfaces:**

```kotlin
// AppSettingsNavigator
fun navigateTo(devices: DevicesNavigator.Args)
fun present(paywall: PaywallNavigator.Args)   // direct-to-paywall route

// AgentBuilderNavigator (if §5.3 option 2)
data class Args(val conversationId: String, val entryMode: AgentBuilderEntryPoint)
```

**Remove methods on existing interfaces:**

```kotlin
// AppSettingsNavigator
fun navigateTo(contacts: ContactsNavigator.Args)              // remove
fun navigateTo(subscriptionSettings: SubscriptionSettingsNavigator.Args) // remove or move to Android-only
```

**Rename interfaces** (paired with iOS collector call site updates):

- `AssistantFilesLinksNavigator` -> `AgentFilesLinksNavigator`
- `AssistantInfoNavigator` -> `AgentInfoNavigator`
- `ProcessingPowerInfoNavigator` -> `AgentPowerInfoNavigator`
- `HtmlAttachmentPreviewNavigator` -> `AttachmentPreviewNavigator`
- (defer) `AssistantConfirmationNavigator`, `AssistantSettingsNavigator`
  pending iOS impl

**Re-source `conversations -> app_settings`** to a shell parent if §5.1
option 1 wins; otherwise leave.

## Appendix B -- iOS-source citation index

For every claim above, the iOS source of truth:

| Claim | File |
|-------|------|
| Three tabs in shell | `Convos/MainTabView.swift:392-399`, `Convos/Conversations List/ConvosTab.swift` |
| Shell-level sheets | `Convos/MainTabView.swift:784-869` |
| App settings overlay trigger | `Convos/MainTabView.swift:483-549,825` |
| Stuff tab root | `Convos/Conversations List/StuffTabView.swift` |
| Stuff detail push | `Convos/Conversations List/StuffTabView.swift:65` |
| Devices view | `Convos/Devices/DevicesView.swift` |
| Pair / remove device sheets | `Convos/Devices/PairingSheetView.swift`, `Convos/Devices/RemoveDeviceSheetView.swift` |
| App settings -> Devices push | `Convos/App Settings/AppSettingsView.swift:130` |
| App settings "Power" -> Paywall direct | `Convos/App Settings/AppSettingsView.swift:232,237` |
| Subscription settings only from debug | `Convos/Debug View/DebugView.swift:119` |
| ContactDetailView modes | `Convos/Contacts/ContactDetailMode.swift` |
| Agent template sections in ContactDetailView | `Convos/Contacts/AgentTemplateConversationsSections.swift`; `Convos/Contacts/ContactDetailView.swift:67,80,111-176,693,709-710` |
| Inline agent builder | `Convos/Agent Builder/AgentBuilderView.swift` (modes); `Convos/Conversations List/ConversationsView.swift` (empty-state swap + `handleInlineBuilderCommit`) |
| Inline share-invite sheet | `Convos/Conversation Detail/ConversationInfoView.swift:83,684-690` |
| Inline explode-confirmation sheet | `Convos/Conversation Detail/ConversationInfoView.swift:79` |
| Agents intro sheet | `presentingAgentsIntro` in `ConversationInfoView.swift` |
| Agent power info | `Convos/Subscription/AgentPowerInfoView.swift` |
| Attachment preview generalized | `AttachmentPreviewSheet` (used across `Convos/Conversation Detail/Messages/...`) |
| Reactions drawer | `Convos/Conversation Detail/Messages/Messages View Controller/Reactions/ReactionsDrawerView.swift` |
| Deep links | `Convos/DeepLinking/DeepLinkHandler.swift` |
| QR scanner still present | `Convos/Conversation Creation/QRScannerView.swift` |
| App Clip root | `ConvosAppClip/AppClipRootView.swift` |

A full inventory and edge list is in `../convos-ios/production-nav.md`.
