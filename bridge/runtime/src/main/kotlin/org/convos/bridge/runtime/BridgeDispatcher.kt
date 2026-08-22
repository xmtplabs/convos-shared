package org.convos.bridge.runtime

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * Platform-independent core of the web bridge: parses a raw request envelope,
 * routes it to the matching [PluginHandler], and renders the response envelope.
 *
 * The Android `ConvosWebBridge` feeds it strings received over
 * `addJavascriptInterface`; the Swift twin of this class lives in
 * `BridgeCore.swift` inside the generated ConvosBridge package.
 */
class BridgeDispatcher(
    handlers: List<PluginHandler>,
    private val onReady: () -> Unit = {},
    /** Invoked when a fire-and-forget notification fails; there is no envelope to reject. */
    private val onNotificationError: (method: String, error: Throwable) -> Unit = { _, _ -> },
    /**
     * Scope in which native->JS event streams are collected. Together with
     * [emitEvent] this enables the event surface; if either is null the bridge
     * silently ignores subscriptions (the platform does not push events).
     */
    private val eventScope: CoroutineScope? = null,
    /** Delivers one event envelope JSON to the page (via `_handleEvent`). */
    private val emitEvent: ((String) -> Unit)? = null,
) {
    private val handlersByPlugin: Map<String, PluginHandler> = handlers.associateBy { it.pluginName }

    /** Active event collectors, keyed by full wire event name (`plugin.function`). */
    private val subscriptions = mutableMapOf<String, Job>()

    /**
     * Dispatches one raw request. Returns the JSON response envelope to deliver
     * back to the page, or null when no response should be sent (notifications
     * and requests whose id cannot be recovered).
     */
    suspend fun dispatch(rawJson: String): String? {
        val request = try {
            bridgeJson.decodeFromString(BridgeRequest.serializer(), rawJson)
        } catch (e: Exception) {
            val id = bestEffortId(rawJson) ?: return null
            return errorResponse(id, BridgeErrorCodes.INVALID_REQUEST, e.message ?: "Unparseable request")
        }

        val result = try {
            invoke(request.method, request.params)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            if (request.id == null) {
                onNotificationError(request.method, e)
                return null
            }
            return when (e) {
                is BridgeMethodNotFoundException ->
                    errorResponse(request.id, BridgeErrorCodes.METHOD_NOT_FOUND, e.message ?: request.method)
                is BridgeInvalidParamsException ->
                    errorResponse(request.id, BridgeErrorCodes.INVALID_PARAMS, e.message ?: "Invalid params")
                else ->
                    errorResponse(request.id, BridgeErrorCodes.PLUGIN_ERROR, e.message ?: (e::class.simpleName ?: "Error"))
            }
        }

        if (request.id == null) return null
        return bridgeJson.encodeToString(
            BridgeResponse.serializer(),
            BridgeResponse(id = request.id, ok = true, result = result),
        )
    }

    private suspend fun invoke(method: String, params: JsonObject): JsonElement {
        when (method) {
            READY_METHOD -> {
                onReady()
                return JsonNull
            }
            SUBSCRIBE_METHOD -> {
                subscribe(params)
                return JsonNull
            }
            UNSUBSCRIBE_METHOD -> {
                unsubscribe(params)
                return JsonNull
            }
        }
        val (pluginName, functionName) = splitMethod(method)
        val handler = handlersByPlugin[pluginName]
            ?: throw BridgeMethodNotFoundException(pluginName, functionName)
        return handler.handle(functionName, params)
    }

    private fun splitMethod(method: String): Pair<String, String> {
        val separator = method.indexOf('.')
        if (separator <= 0 || separator == method.length - 1) {
            throw BridgeMethodNotFoundException("?", method)
        }
        return method.substring(0, separator) to method.substring(separator + 1)
    }

    /** Starts collecting the requested event stream, pushing each emission to the page. */
    private fun subscribe(params: JsonObject) {
        val scope = eventScope ?: return
        val emit = emitEvent ?: return
        val event = params.eventName()
        val (pluginName, functionName) = splitMethod(event)
        val handler = handlersByPlugin[pluginName]
            ?: throw BridgeMethodNotFoundException(pluginName, functionName)
        val flow = handler.events(functionName)
            ?: throw BridgeMethodNotFoundException(pluginName, functionName)
        synchronized(subscriptions) {
            if (subscriptions.containsKey(event)) return
            subscriptions[event] = scope.launch {
                flow.collect { payload ->
                    emit(
                        bridgeJson.encodeToString(BridgeEvent.serializer(), BridgeEvent(event, payload)),
                    )
                }
            }
        }
    }

    private fun unsubscribe(params: JsonObject) {
        val event = params.eventName()
        synchronized(subscriptions) {
            subscriptions.remove(event)?.cancel()
        }
    }

    private fun JsonObject.eventName(): String =
        (this["event"] as? JsonPrimitive)?.contentOrNull
            ?: throw BridgeInvalidParamsException("Missing 'event' name for subscription")

    private fun errorResponse(id: String, code: String, message: String): String =
        bridgeJson.encodeToString(
            BridgeResponse.serializer(),
            BridgeResponse(id = id, ok = false, error = BridgeErrorPayload(code, message)),
        )

    /** Recovers the request id from an envelope that failed strict decoding. */
    private fun bestEffortId(rawJson: String): String? = try {
        val element = bridgeJson.parseToJsonElement(rawJson)
        ((element as? JsonObject)?.get("id") as? JsonPrimitive)?.contentOrNull
    } catch (_: Exception) {
        null
    }

    companion object {
        /** Reserved method the page sends from `window.convos.onReady()`. */
        const val READY_METHOD = "_bridge.ready"

        /** Reserved methods the page sends when (un)subscribing to an event. */
        const val SUBSCRIBE_METHOD = "_bridge.subscribe"
        const val UNSUBSCRIBE_METHOD = "_bridge.unsubscribe"
    }
}
