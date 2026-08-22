package org.convos.bridge.plugins

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import org.convos.bridge.annotations.BridgePlugin

/**
 * The bridge surface exposed to JavaScript. These interfaces are the single
 * source of truth: the codegen processor turns them into Kotlin handlers, the
 * ConvosBridge Swift package, and the methods on `window.convos` in convos.js.
 *
 * Native apps implement these interfaces (Kotlin here, the generated Swift
 * protocols on iOS) and hand them to `ConvosWebBridge`.
 *
 * Method kinds:
 *  - `suspend fun`  -> request/response (`convos.foo()` returns a Promise),
 *  - non-suspend `fun` returning `Unit` -> fire-and-forget notification,
 *  - non-suspend `fun` returning `Flow<T>` -> native->JS event stream
 *    (`convos.foo(cb)` registers a callback and returns an unsubscribe fn).
 */
@BridgePlugin
interface EventsPlugin {
    /** Fire-and-forget analytics event; never produces a response. */
    fun trackEvent(name: String)
}

/** App lifecycle signals from web to the native shell. */
@BridgePlugin
interface AppPlugin {
    /**
     * Web signals it has actually painted and the native splash/cover may be
     * hidden. Distinct from the top-level `convos.onReady()` (`_bridge.ready`),
     * which only means the bridge JS has bootstrapped.
     */
    fun markReady()
}

/** Open native invite / onboarding UI. */
@BridgePlugin
interface InvitePlugin {
    fun showShareSheet()

    fun showScan()

    fun showInviteCode()

    fun showInvitePicker()
}

/** Conversation-scoped native UI and agent membership. */
@BridgePlugin
interface ChatPlugin {
    fun showMembersList()

    /** Current agent membership status for the active conversation. */
    suspend fun getAgentStatus(): AgentStatus

    /**
     * Ask the native side to add the agent. Fire-and-forget: progress and the
     * terminal state arrive through [onAgentStatusChanged].
     */
    fun requestAgentJoin()

    /**
     * Emitted whenever agent status changes; surfaces in JS as
     * `convos.onAgentStatusChanged(cb)` returning an unsubscribe function.
     */
    fun onAgentStatusChanged(): Flow<AgentStatus>
}

/** The "things" list surface. */
@BridgePlugin
interface ThingsPlugin {
    fun showThingsList()

    suspend fun getThingsList(): List<ThingInfo>
}

/** Desktop-only widget interactions. */
@BridgePlugin
interface DesktopPlugin {
    /**
     * Attach a message to the agent compose bar referencing a widget
     * ("talk to this"). Fire-and-forget.
     */
    fun replyToWidget(id: String, widget: WidgetRef)

    /**
     * Ask native to present a widget popup with a nicer animation. Callers fall
     * back to `location.href = url` when the native bridge is unavailable.
     */
    fun popupWidget(id: String, url: String, bounds: PopupBounds)
}

@Serializable
enum class AgentState {
    NONE,
    JOINING,
    ACTIVE,
}

/**
 * Flattened form of `{ none, joining(progress), active }`. [progress] is 0.0–1.0
 * and is non-null only when [state] is [AgentState.JOINING].
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
 * TODO: placeholder shape — fill in the real fields (icon, kind, conversationId,
 * timestamps, …) once the things model is settled.
 */
@Serializable
data class ThingInfo(
    val id: String,
    val title: String,
)
