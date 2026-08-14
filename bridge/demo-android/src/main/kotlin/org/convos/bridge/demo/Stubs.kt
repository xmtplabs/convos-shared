package org.convos.bridge.demo

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.convos.bridge.plugins.AgentState
import org.convos.bridge.plugins.AgentStatus
import org.convos.bridge.plugins.AppPlugin
import org.convos.bridge.plugins.ChatPlugin
import org.convos.bridge.plugins.DesktopPlugin
import org.convos.bridge.plugins.EventsPlugin
import org.convos.bridge.plugins.InvitePlugin
import org.convos.bridge.plugins.PopupBounds
import org.convos.bridge.plugins.ThingInfo
import org.convos.bridge.plugins.ThingsPlugin
import org.convos.bridge.plugins.WidgetRef

/** Stub implementations backing the demo page. */
class DemoEvents : EventsPlugin {
    override fun trackEvent(name: String) {
        Log.i(TAG, "trackEvent: $name")
    }
}

class DemoApp : AppPlugin {
    override fun markReady() {
        Log.i(TAG, "markReady")
    }
}

class DemoInvite : InvitePlugin {
    override fun showShareSheet() { Log.i(TAG, "showShareSheet") }
    override fun showScan() { Log.i(TAG, "showScan") }
    override fun showInviteCode() { Log.i(TAG, "showInviteCode") }
    override fun showInvitePicker() { Log.i(TAG, "showInvitePicker") }
}

class DemoChat : ChatPlugin {
    private val status = MutableStateFlow(AgentStatus(AgentState.NONE))

    override fun showMembersList() {
        Log.i(TAG, "showMembersList")
    }

    override suspend fun getAgentStatus(): AgentStatus = status.value

    override fun requestAgentJoin() {
        status.update { AgentStatus(AgentState.JOINING, 0.0) }
    }

    override fun onAgentStatusChanged(): Flow<AgentStatus> = status
}

class DemoThings : ThingsPlugin {
    override fun showThingsList() {
        Log.i(TAG, "showThingsList")
    }

    override suspend fun getThingsList(): List<ThingInfo> =
        listOf(ThingInfo(id = "thing-1", title = "Demo thing"))
}

class DemoDesktop : DesktopPlugin {
    override fun replyToWidget(id: String, widget: WidgetRef) {
        Log.i(TAG, "replyToWidget($id, ${widget.title})")
    }

    override fun popupWidget(id: String, url: String, bounds: PopupBounds) {
        Log.i(TAG, "popupWidget($id, $url)")
    }
}

private const val TAG = "ConvosBridgeDemo"
