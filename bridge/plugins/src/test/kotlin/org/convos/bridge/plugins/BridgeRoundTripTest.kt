package org.convos.bridge.plugins

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.convos.bridge.generated.bridgeHandlers
import org.convos.bridge.runtime.BridgeDispatcher
import org.junit.jupiter.api.Test

/**
 * Exercises the actual KSP-generated handlers end to end: raw request envelope
 * in, exact response envelope out.
 */
class BridgeRoundTripTest {

    private class RecordingEvents : EventsPlugin {
        val names = mutableListOf<String>()
        override fun trackEvent(name: String) {
            names += name
        }
    }

    private class NoopApp : AppPlugin {
        override fun markReady() = Unit
    }

    private class NoopInvite : InvitePlugin {
        override fun showShareSheet() = Unit
        override fun showScan() = Unit
        override fun showInviteCode() = Unit
        override fun showInvitePicker() = Unit
    }

    private class FakeThings : ThingsPlugin {
        override fun showThingsList() = Unit
        override suspend fun getThingsList(): List<ThingInfo> =
            listOf(ThingInfo("t1", "First thing"), ThingInfo("t2", "Second thing"))
    }

    private class RecordingDesktop : DesktopPlugin {
        val replies = mutableListOf<Pair<String, WidgetRef>>()
        override fun replyToWidget(id: String, widget: WidgetRef) {
            replies += id to widget
        }

        override fun popupWidget(id: String, url: String, bounds: PopupBounds) = Unit
    }

    /** Agent status is driven by a StateFlow so tests can push transitions. */
    private class FakeChat : ChatPlugin {
        val status = MutableStateFlow(AgentStatus(AgentState.NONE))
        var failure: Throwable? = null

        override fun showMembersList() = Unit
        override fun showAgentDm() = Unit
        override suspend fun getAgentStatus(): AgentStatus = failure?.let { throw it } ?: status.value
        override fun requestAgentJoin() {
            status.value = AgentStatus(AgentState.JOINING, 0.0)
        }

        override fun onAgentStatusChanged(): Flow<AgentStatus> = status
    }

    private val events = RecordingEvents()
    private val desktop = RecordingDesktop()
    private val chat = FakeChat()

    private fun handlers() = bridgeHandlers(
        app = NoopApp(),
        chat = chat,
        desktop = desktop,
        events = events,
        invite = NoopInvite(),
        things = FakeThings(),
    )

    private val dispatcher = BridgeDispatcher(handlers = handlers())

    @Test
    fun `structured return with omitted null field and enum value`() = runTest {
        val response = dispatcher.dispatch("""{"id":"1","method":"chat.getAgentStatus","params":{}}""")
        assertThat(response).isEqualTo("""{"id":"1","ok":true,"result":{"state":"NONE"}}""")
    }

    @Test
    fun `structured return with populated nullable number field`() = runTest {
        chat.status.value = AgentStatus(AgentState.JOINING, 0.5)
        val response = dispatcher.dispatch("""{"id":"2","method":"chat.getAgentStatus","params":{}}""")
        assertThat(response).isEqualTo("""{"id":"2","ok":true,"result":{"state":"JOINING","progress":0.5}}""")
    }

    @Test
    fun `list-of-struct return`() = runTest {
        val response = dispatcher.dispatch("""{"id":"3","method":"things.getThingsList","params":{}}""")
        assertThat(response).isEqualTo(
            """{"id":"3","ok":true,"result":[""" +
                """{"id":"t1","title":"First thing"},""" +
                """{"id":"t2","title":"Second thing"}]}""",
        )
    }

    @Test
    fun `nested struct parameter decodes`() = runTest {
        val response = dispatcher.dispatch(
            """{"id":null,"method":"desktop.replyToWidget",""" +
                """"params":{"id":"w1","widget":{"title":"T","description":"D"}}}""",
        )
        assertThat(response).isNull()
        assertThat(desktop.replies).containsExactly("w1" to WidgetRef("T", "D"))
    }

    @Test
    fun `missing struct parameter maps to INVALID_PARAMS`() = runTest {
        val response = dispatcher.dispatch("""{"id":"4","method":"desktop.replyToWidget","params":{"id":"w1"}}""")
        assertThat(response).contains(""""ok":false""")
        assertThat(response).contains(""""code":"INVALID_PARAMS"""")
        assertThat(response).contains("widget")
    }

    @Test
    fun `implementation exception maps to PLUGIN_ERROR`() = runTest {
        chat.failure = IllegalStateException("kaboom")
        val response = dispatcher.dispatch("""{"id":"5","method":"chat.getAgentStatus","params":{}}""")
        assertThat(response)
            .isEqualTo("""{"id":"5","ok":false,"error":{"code":"PLUGIN_ERROR","message":"kaboom"}}""")
    }

    @Test
    fun `notification runs the implementation and yields no response`() = runTest {
        val response = dispatcher.dispatch("""{"id":null,"method":"events.trackEvent","params":{"name":"tapped"}}""")
        assertThat(response).isNull()
        assertThat(events.names).containsExactly("tapped")
    }

    @Test
    fun `unknown method maps to METHOD_NOT_FOUND`() = runTest {
        val response = dispatcher.dispatch("""{"id":"6","method":"chat.nope","params":{}}""")
        assertThat(response).contains(""""code":"METHOD_NOT_FOUND"""")
    }

    @Test
    fun `event subscription pushes envelopes to the page and stops on unsubscribe`() = runTest {
        val emitted = mutableListOf<String>()
        val eventDispatcher = BridgeDispatcher(
            handlers = handlers(),
            eventScope = backgroundScope,
            emitEvent = { emitted += it },
        )

        eventDispatcher.dispatch(
            """{"id":null,"method":"_bridge.subscribe","params":{"event":"chat.onAgentStatusChanged"}}""",
        )
        runCurrent()
        // StateFlow replays its current value to the new subscriber.
        assertThat(emitted).containsExactly(
            """{"event":"chat.onAgentStatusChanged","params":{"state":"NONE"}}""",
        )

        chat.status.value = AgentStatus(AgentState.JOINING, 0.5)
        runCurrent()
        assertThat(emitted.last())
            .isEqualTo("""{"event":"chat.onAgentStatusChanged","params":{"state":"JOINING","progress":0.5}}""")

        eventDispatcher.dispatch(
            """{"id":null,"method":"_bridge.unsubscribe","params":{"event":"chat.onAgentStatusChanged"}}""",
        )
        runCurrent()
        val countAfterUnsub = emitted.size
        chat.status.value = AgentStatus(AgentState.ACTIVE)
        runCurrent()
        assertThat(emitted).hasSize(countAfterUnsub)
    }
}
