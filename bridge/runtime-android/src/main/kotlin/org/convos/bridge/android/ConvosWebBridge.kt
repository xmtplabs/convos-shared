package org.convos.bridge.android

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.webkit.ScriptHandler
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.convos.bridge.runtime.BridgeDispatcher
import org.convos.bridge.runtime.PluginHandler
import org.convos.bridge.runtime.jsStringLiteral

/**
 * Wires the Convos web bridge into an Android [WebView]: receives request
 * envelopes from `window.convos` through an injected `ConvosNative` object,
 * dispatches them to the plugin implementations, and evaluates the response
 * back into the page.
 *
 * ```kotlin
 * val bridge = ConvosWebBridge(webView, bridgeHandlers(...), lifecycleScope)
 * bridge.onReady = { Log.i(TAG, "page is ready") }
 * bridge.attach()                                  // before loadUrl(...)
 * webView.loadUrl("file:///android_asset/index.html")
 * ```
 */
class ConvosWebBridge(
    private val webView: WebView,
    handlers: List<PluginHandler>,
    private val scope: CoroutineScope,
) {
    /** Invoked when the page calls `window.convos.onReady()`. */
    var onReady: (() -> Unit)? = null

    /** Invoked when a fire-and-forget notification fails; defaults to a log line. */
    var onNotificationError: ((method: String, error: Throwable) -> Unit)? = null

    /** Handle to the injected document-start script, so [detach] can remove it. */
    private var startupScript: ScriptHandler? = null

    private val dispatcher = BridgeDispatcher(
        handlers = handlers,
        onReady = { onReady?.invoke() },
        onNotificationError = { method, error ->
            onNotificationError?.invoke(method, error)
                ?: Log.w(TAG, "Bridge notification '$method' failed", error)
        },
        eventScope = scope,
        emitEvent = { envelope ->
            scope.launch(Dispatchers.Main) {
                webView.evaluateJavascript(
                    "window.convos._handleEvent(JSON.parse(${jsStringLiteral(envelope)}))",
                    null,
                )
            }
        },
    )

    /**
     * Registers the JavaScript interface, enables JavaScript, and injects
     * `convos.js` so `window.convos` exists before any page script runs. Call
     * before loading page content.
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun attach() {
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(NativeInterface(), JS_OBJECT_NAME)
        injectConvosScript()
    }

    fun detach() {
        webView.removeJavascriptInterface(JS_OBJECT_NAME)
        startupScript?.remove()
        startupScript = null
    }

    /**
     * Installs `window.convos` as a document-start script, so pages can use the
     * bridge from their first inline `<script>` without loading it. The script
     * is bundled as an asset of `runtime-android` and merged into the host app.
     */
    private fun injectConvosScript() {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
            Log.w(TAG, "WebView lacks DOCUMENT_START_SCRIPT; window.convos will not be pre-injected")
            return
        }
        val source = webView.context.assets.open(CONVOS_JS_ASSET)
            .bufferedReader().use { it.readText() }
        startupScript = WebViewCompat.addDocumentStartJavaScript(webView, source, setOf("*"))
    }

    private inner class NativeInterface {
        @JavascriptInterface
        fun postMessage(json: String) {
            // Arrives on a WebView-internal thread; run the handler on the
            // provided scope and marshal the response back to the main thread.
            scope.launch {
                val response = dispatcher.dispatch(json) ?: return@launch
                withContext(Dispatchers.Main) {
                    webView.evaluateJavascript(
                        "window.convos._handleResponse(JSON.parse(${jsStringLiteral(response)}))",
                        null,
                    )
                }
            }
        }
    }

    companion object {
        private const val TAG = "ConvosWebBridge"

        /** Name of the object convos.js looks for on `window`. */
        const val JS_OBJECT_NAME = "ConvosNative"

        /** Bundled convos.js asset, merged into the host app from this library. */
        private const val CONVOS_JS_ASSET = "convos.js"
    }
}
