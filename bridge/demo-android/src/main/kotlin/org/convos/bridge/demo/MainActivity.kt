package org.convos.bridge.demo

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import org.convos.bridge.android.ConvosWebBridge
import org.convos.bridge.generated.bridgeHandlers

class MainActivity : Activity() {

    private val scope = MainScope()
    private lateinit var bridge: ConvosWebBridge

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val webView = WebView(this)
        bridge = ConvosWebBridge(
            webView = webView,
            handlers = bridgeHandlers(
                app = DemoApp(),
                chat = DemoChat(),
                desktop = DemoDesktop(),
                events = DemoEvents(),
                invite = DemoInvite(),
                things = DemoThings(),
            ),
            scope = scope,
        )
        bridge.onReady = { Log.i(TAG, "convos.js reported ready") }
        bridge.attach()

        setContentView(webView)
        webView.loadUrl("file:///android_asset/index.html")
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "ConvosBridgeDemo"
    }
}
