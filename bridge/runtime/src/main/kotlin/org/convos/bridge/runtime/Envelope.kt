package org.convos.bridge.runtime

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * A call from JavaScript. `id == null` marks a fire-and-forget notification:
 * no response envelope is ever sent back for it.
 */
@Serializable
data class BridgeRequest(
    val id: String? = null,
    val method: String,
    val params: JsonObject = JsonObject(emptyMap()),
)

@Serializable
data class BridgeErrorPayload(
    val code: String,
    val message: String,
)

/**
 * The reply pushed back into the page via `window.convos._handleResponse(...)`.
 * Exactly one of [result] / [error] is populated, keyed by [ok].
 */
@Serializable
data class BridgeResponse(
    val id: String,
    val ok: Boolean,
    val result: JsonElement? = null,
    val error: BridgeErrorPayload? = null,
)

/**
 * A native->JS event, pushed into the page via `window.convos._handleEvent(...)`.
 * [event] is the full wire name (`plugin.function`) the page subscribed to.
 */
@Serializable
data class BridgeEvent(
    val event: String,
    val params: JsonElement,
)

object BridgeErrorCodes {
    const val INVALID_REQUEST = "INVALID_REQUEST"
    const val METHOD_NOT_FOUND = "METHOD_NOT_FOUND"
    const val INVALID_PARAMS = "INVALID_PARAMS"
    const val PLUGIN_ERROR = "PLUGIN_ERROR"
}

val bridgeJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}
