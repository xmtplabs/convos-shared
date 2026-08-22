# Bridge plugin contracts — desktop/web surface

> **Status: implemented** (2026-08-13). The `@BridgePlugin` interfaces below now
> live in `bridge/plugins/src/main/kotlin/org/convos/bridge/plugins/Plugins.kt`,
> the single source of truth; `cd bridge && ./gradlew build` regenerates the
> Kotlin handlers, the `ConvosBridge` Swift package, and `window.convos` in
> `web/convos.js`. The native→JS event primitive in §2 is implemented across the
> codegen, the Kotlin/Android runtime, and the Swift runtime.
>
> **The JS surface is flat**, not namespaced: `convos.showScan()`,
> `convos.markReady()`, `convos.onAgentStatusChanged(cb)` — *not*
> `convos.invite.showScan()`. Method names are therefore globally unique across
> all plugins (enforced by the processor). The `plugin` segment survives only in
> the wire `method` (`invite.showScan`) and event names (`chat.onAgentStatusChanged`).

Framework recap (from `bridge/annotations/.../BridgePlugin.kt`):

- `suspend fun` → request/response. Non-suspend `fun` returning `Unit` →
  fire-and-forget notification. Non-suspend `fun` returning `Flow<T>` →
  native→JS event stream (§2).
- Interface name must end in `Plugin`; the wire plugin name is the lowerCamel
  simple name minus that suffix (`InvitePlugin` → wire `invite`). The JS method
  is flat: the function name alone (`showScan`).
- Param/return types: primitives, enums, `@Serializable` classes, `List<T>`
  of those, `Flow<T>` (event return only), and nullable variants.

Decisions baked in (2026-08-13):

- `onAgentStatusChanged` → **add a first-class native→JS event primitive** (§2).
- `AgentStatus` → **flat data class**, not a codegen sum type (§1, `AgentStatus`).
- All `show*` / navigate-to-native methods → **fire-and-forget** (non-suspend `Unit`).
- `ThingInfo` → **placeholder** `{ id, title }` with a TODO to fill in.

---

## 1. Plugin interfaces

```kotlin
package org.convos.bridge.plugins

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import org.convos.bridge.annotations.BridgePlugin

/** convos.app — lifecycle signals from web to the native shell. */
@BridgePlugin
interface AppPlugin {
    /**
     * Web signals it has actually painted and the native splash/cover may be
     * hidden. Distinct from the existing top-level `convos.onReady()` /
     * `_bridge.ready`, which only means the bridge JS has bootstrapped — this
     * means the page content is ready to show.
     */
    fun markReady()
}

/** convos.invite — open native invite/onboarding UI. */
@BridgePlugin
interface InvitePlugin {
    fun showShareSheet()
    fun showScan()
    fun showInviteCode()
    fun showInvitePicker()
}

/** convos.chat — conversation-scoped native UI and agent membership. */
@BridgePlugin
interface ChatPlugin {
    fun showMembersList()

    /** Current agent membership status for the active conversation. */
    suspend fun getAgentStatus(): AgentStatus

    /**
     * Ask the native side to add the agent. Fire-and-forget: progress and the
     * terminal state arrive through [onAgentStatusChanged] (§2), not a return
     * value.
     */
    fun requestAgentJoin()

    /**
     * Event stream (§2). Emitted whenever agent status changes. Surfaces in JS
     * as `convos.chat.onAgentStatusChanged(cb)` returning an unsubscribe fn.
     * NOTE: requires the event primitive in §2 before this compiles through
     * the codegen.
     */
    fun onAgentStatusChanged(): Flow<AgentStatus>
}

/** convos.things — the "things" list surface. */
@BridgePlugin
interface ThingsPlugin {
    fun showThingsList()
    suspend fun getThingsList(): List<ThingInfo>
}

/** convos.desktop — desktop-only widget interactions. */
@BridgePlugin
interface DesktopPlugin {
    /**
     * Attach a message to the agent compose bar referencing a widget
     * ("talk to this"). Fire-and-forget.
     */
    fun replyToWidget(id: String, widget: WidgetRef)

    /**
     * Ask native to present a widget popup with a nicer animation. JS calls
     * this first and falls back to `location.href = url` if the native call is
     * unavailable — see §3 for how the fallback is wired (it is NOT part of the
     * generated wrapper).
     */
    fun popupWidget(id: String, url: String, bounds: PopupBounds)
}
```

### Shared types

```kotlin
@Serializable
enum class AgentState {
    NONE,
    JOINING,
    ACTIVE,
}

/**
 * Flattened form of `{ none, joining(progress), active }`.
 * [progress] is 0.0–1.0 and is non-null only when [state] == JOINING.
 */
@Serializable
data class AgentStatus(
    val state: AgentState,
    val progress: Double? = null,
)

/** Payload attached to the compose bar by `replyToWidget`. */
@Serializable
data class WidgetRef(
    val title: String,
    val description: String,
)

/** CSS-pixel geometry for `popupWidget`, relative to the web viewport. */
@Serializable
data class PopupBounds(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
)

/**
 * TODO: placeholder shape. Fill in the real fields (icon, kind, conversationId,
 * timestamps, …) once the things model is settled.
 */
@Serializable
data class ThingInfo(
    val id: String,
    val title: String,
)
```

### JS surface produced (flat)

```js
convos.markReady()                        // void (notification)

convos.showShareSheet()                   // void
convos.showScan()                         // void
convos.showInviteCode()                   // void
convos.showInvitePicker()                 // void

convos.showMembersList()                  // void
convos.getAgentStatus()                   // Promise<AgentStatus>
convos.requestAgentJoin()                 // void
convos.onAgentStatusChanged(cb)           // () => void  (unsubscribe) — §2

convos.showThingsList()                   // void
convos.getThingsList()                    // Promise<ThingInfo[]>

convos.replyToWidget(id, { title, description })     // void
convos.popupWidget(id, url, { x, y, width, height }) // void — §3
```

---

## 2. Event primitive (native → JS push)

The bridge today only sends response envelopes back to the page. `AgentStatus`
change notifications need native to *push* to a subscribing page. This section
defines that primitive; it is a framework addition, reusable by any future
event.

### 2.1 Wire protocol

Three envelope shapes now cross the bridge:

| Direction   | Shape                                               | Purpose            |
|-------------|-----------------------------------------------------|--------------------|
| JS → native | `{ id, method, params }`                            | request            |
| JS → native | `{ id: null, method, params }`                      | notification       |
| native → JS | `{ id, ok, result \| error }`                       | response (exists)  |
| **native → JS** | **`{ event: "chat.onAgentStatusChanged", params: {...} }`** | **event (new)** |

An event envelope has no `id` and no `ok`; JS discriminates on the presence of
the `event` key. To let native avoid emitting into a page with no listeners,
subscription is signalled with two reserved notifications:

| JS → native notification                              | Meaning                    |
|-------------------------------------------------------|----------------------------|
| `{ id: null, method: "_bridge.subscribe",   params: { event } }` | first listener attached |
| `{ id: null, method: "_bridge.unsubscribe", params: { event } }` | last listener detached  |

(A simpler variant drops subscribe/unsubscribe and has native always emit; the
subscribe pair is preferred so native only does work while the page cares.)

### 2.2 JS surface

`window.convos` gains an event registry and an `_handleEvent` entry point
(twin of the existing `_handleResponse`):

```js
onAgentStatusChanged(cb) {
  return this._subscribe("chat.onAgentStatusChanged", cb); // returns unsubscribe fn
}

// internal
_subscribe(event, cb) {
  let set = this._listeners.get(event);
  if (!set) { set = new Set(); this._listeners.set(event, set);
              this._notify("_bridge.subscribe", { event }); }
  set.add(cb);
  return () => {
    set.delete(cb);
    if (set.size === 0) { this._listeners.delete(event);
                          this._notify("_bridge.unsubscribe", { event }); }
  };
}

_handleEvent(envelope) {                    // called by native, like _handleResponse
  const set = this._listeners.get(envelope.event);
  if (set) for (const cb of set) cb(envelope.params);
}
```

### 2.3 Codegen convention (implemented)

An **event** is declared as a non-suspend interface function named exactly like
its JS subscription, taking no parameters and returning `Flow<T>` where `T` is a
bridge-serializable type:

```kotlin
fun onAgentStatusChanged(): Flow<AgentStatus>
```

The name is preserved verbatim across all three surfaces. What each target
emits:

- **Kotlin runtime** — the generated `PluginHandler` overrides
  `events(name): Flow<JsonElement>?`, mapping the impl's `Flow<T>` through
  `bridgeJson.encodeToJsonElement`. On `_bridge.subscribe`, `BridgeDispatcher`
  collects it on the supplied `eventScope` and pushes each emission via the
  `emitEvent` sink; on `_bridge.unsubscribe` it cancels the collector. The
  Android `ConvosWebBridge` supplies `eventScope` + an `emitEvent` that runs
  `evaluateJavascript("window.convos._handleEvent(…)")` on the main thread.
- **Swift** — the protocol method is
  `func onAgentStatusChanged() -> AsyncStream<AgentStatus>`; the generated
  dispatcher implements `events(_:) -> AsyncStream<Any>?`, and the Swift
  `BridgeDispatcher` iterates it in a `Task`, forwarding to an `emitEvent`
  closure that `ConvosWebBridge` wires to `_handleEvent`.
- **convos.js** — the `onXxx(cb)` subscription method (§2.2), plus the
  `_listeners` registry, `_handleEvent`, and `_subscribe` in the runtime prelude.

Model/runtime changes that landed: `TypeKind.FLOW` (element carried like `LIST`);
`BridgeExtractor` classifies a `Flow`-returning function as an event (and rejects
`suspend`/parameterized event functions); `FunctionDescriptor.isEvent`; the
`_bridge.subscribe` / `_bridge.unsubscribe` reserved methods and a `BridgeEvent`
envelope in both `BridgeDispatcher`s; and `kotlinx-coroutines-core` promoted to
an `api` dependency of the `:runtime` module.

---

## 3. `popupWidget` JS fallback

`popupWidget` should degrade to `location.href` when no native handler is
present. The generated wrappers are uniform native calls, so the fallback can't
live in the generated method. Two ways to wire it:

1. **Hand-authored overlay (recommended).** The generated method stays
   `convos.popupWidget(...)` (plain notification). A small hand-written module
   wraps it:

   ```js
   export function popupWidget(id, url, bounds) {
     try {
       if (typeof window.convos?.popupWidget === "function") {
         window.convos.popupWidget(id, url, bounds);
         return;
       }
     } catch (_) { /* fall through */ }
     location.href = url;
   }
   ```

   Keeps codegen uniform; callers import the wrapper instead of touching
   `window.convos.popupWidget` directly.

2. **Codegen escape hatch.** Add an annotation (e.g. `@JsFallback`) the
   processor honors to emit a hand-written body. More machinery for a single
   call site; only worth it if more methods need JS-side fallbacks.

Recommendation: option 1 unless a second fallback appears.

---

## 4. Open items

- **`ThingInfo` fields** — placeholder `{ id, title }`; fill in before shipping
  `getThingsList`.
- **`markReady` vs `onReady`** — confirm the native shell wants both signals
  (bridge-bootstrapped vs content-painted) or whether `markReady` should
  supersede the splash-hide currently keyed off `_bridge.ready`.
- **Subscribe/emit lifecycle** — confirm native only needs to emit
  `onAgentStatusChanged` for the active conversation, and how it should behave
  across conversation switches (re-emit current status on subscribe?).
- **`showScan` / `showInvitePicker` results** — currently fire-and-forget. If a
  scanned code or picked selection must flow back to web, revisit as `suspend`
  returning a result type (or an event).
