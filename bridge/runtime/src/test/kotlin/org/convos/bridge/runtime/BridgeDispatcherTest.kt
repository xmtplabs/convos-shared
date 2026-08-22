package org.convos.bridge.runtime

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Test

class BridgeDispatcherTest {

    private class FakeHandler(
        override val pluginName: String = "fake",
        private val onHandle: suspend (String, JsonObject) -> JsonElement,
    ) : PluginHandler {
        override suspend fun handle(method: String, params: JsonObject): JsonElement =
            onHandle(method, params)
    }

    private fun dispatcher(
        handler: PluginHandler = FakeHandler { method, _ ->
            if (method == "echo") JsonPrimitive("echoed") else throw BridgeMethodNotFoundException("fake", method)
        },
        onReady: () -> Unit = {},
        onNotificationError: (String, Throwable) -> Unit = { _, _ -> },
    ) = BridgeDispatcher(listOf(handler), onReady, onNotificationError)

    @Test
    fun `successful call returns ok envelope`() = runTest {
        val response = dispatcher().dispatch("""{"id":"1","method":"fake.echo","params":{}}""")
        assertThat(response).isEqualTo("""{"id":"1","ok":true,"result":"echoed"}""")
    }

    @Test
    fun `params are forwarded to the handler`() = runTest {
        var seen: JsonObject? = null
        val handler = FakeHandler { _, params -> seen = params; JsonNull }
        dispatcher(handler).dispatch("""{"id":"1","method":"fake.echo","params":{"a":1}}""")
        assertThat(seen?.get("a")).isEqualTo(JsonPrimitive(1))
    }

    @Test
    fun `handler exception maps to PLUGIN_ERROR`() = runTest {
        val handler = FakeHandler { _, _ -> throw RuntimeException("boom") }
        val response = dispatcher(handler).dispatch("""{"id":"2","method":"fake.echo","params":{}}""")
        assertThat(response)
            .isEqualTo("""{"id":"2","ok":false,"error":{"code":"PLUGIN_ERROR","message":"boom"}}""")
    }

    @Test
    fun `unknown plugin maps to METHOD_NOT_FOUND`() = runTest {
        val response = dispatcher().dispatch("""{"id":"3","method":"nope.echo","params":{}}""")
        assertThat(response).contains(""""code":"METHOD_NOT_FOUND"""")
    }

    @Test
    fun `unknown method maps to METHOD_NOT_FOUND`() = runTest {
        val response = dispatcher().dispatch("""{"id":"4","method":"fake.nope","params":{}}""")
        assertThat(response).contains(""""code":"METHOD_NOT_FOUND"""")
    }

    @Test
    fun `method without namespace maps to METHOD_NOT_FOUND`() = runTest {
        val response = dispatcher().dispatch("""{"id":"5","method":"echo","params":{}}""")
        assertThat(response).contains(""""code":"METHOD_NOT_FOUND"""")
    }

    @Test
    fun `invalid params map to INVALID_PARAMS`() = runTest {
        val handler = FakeHandler { _, _ -> throw BridgeInvalidParamsException("Invalid value for parameter 'x'") }
        val response = dispatcher(handler).dispatch("""{"id":"6","method":"fake.echo","params":{}}""")
        assertThat(response).contains(""""code":"INVALID_PARAMS"""")
    }

    @Test
    fun `envelope with recoverable id but missing method maps to INVALID_REQUEST`() = runTest {
        val response = dispatcher().dispatch("""{"id":"7"}""")
        assertThat(response).contains(""""id":"7"""")
        assertThat(response).contains(""""code":"INVALID_REQUEST"""")
    }

    @Test
    fun `unparseable envelope without id yields no response`() = runTest {
        assertThat(dispatcher().dispatch("""{"id":""")).isNull()
        assertThat(dispatcher().dispatch("not json at all")).isNull()
    }

    @Test
    fun `notification yields no response`() = runTest {
        val response = dispatcher().dispatch("""{"id":null,"method":"fake.echo","params":{}}""")
        assertThat(response).isNull()
    }

    @Test
    fun `failing notification yields no response but reports the error`() = runTest {
        var reported: Pair<String, Throwable>? = null
        val handler = FakeHandler { _, _ -> throw RuntimeException("boom") }
        val response = dispatcher(handler, onNotificationError = { m, e -> reported = m to e })
            .dispatch("""{"id":null,"method":"fake.echo","params":{}}""")
        assertThat(response).isNull()
        assertThat(reported?.first).isEqualTo("fake.echo")
        assertThat(reported?.second).hasMessageThat().isEqualTo("boom")
    }

    @Test
    fun `ready notification invokes onReady and yields no response`() = runTest {
        var ready = false
        val response = dispatcher(onReady = { ready = true })
            .dispatch("""{"id":null,"method":"_bridge.ready","params":{}}""")
        assertThat(response).isNull()
        assertThat(ready).isTrue()
    }
}
