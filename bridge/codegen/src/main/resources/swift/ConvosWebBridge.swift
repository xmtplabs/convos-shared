// Static runtime for the ConvosBridge package. Copied verbatim into the
// generated Swift package by the bridge codegen processor; edit the copy in
// bridge/codegen/src/main/resources/swift/ instead.

#if canImport(WebKit)
import Foundation
import WebKit

/// Wires the Convos web bridge into a `WKWebView`: receives request envelopes
/// from `window.convos` through a script message handler named "convos",
/// dispatches them to the plugin implementations, and evaluates the response
/// back into the page.
///
/// ```swift
/// let bridge = ConvosWebBridge(plugins: [ChatPluginDispatcher(impl)])
/// bridge.onReady = { print("page is ready") }
/// bridge.attach(to: webView)   // before loading content
/// ```
public final class ConvosWebBridge: NSObject, WKScriptMessageHandler {
    public static let messageHandlerName = "convos"

    /// Invoked when the page calls `window.convos.onReady()`.
    public var onReady: (() -> Void)?

    private var dispatcher: BridgeDispatcher!
    private weak var webView: WKWebView?

    public init(plugins: [BridgePluginDispatcher]) {
        super.init()
        self.dispatcher = BridgeDispatcher(
            plugins: plugins,
            onReady: { [weak self] in
                self?.onReady?()
            },
            emitEvent: { [weak self] envelope in
                self?.emit(envelope)
            }
        )
    }

    /// Pushes a native event envelope into the page via `_handleEvent`.
    private func emit(_ envelope: String) {
        let script = "window.convos._handleEvent(JSON.parse(\(jsStringLiteral(envelope))))"
        Task { @MainActor [weak self] in
            self?.webView?.evaluateJavaScript(script, completionHandler: nil)
        }
    }

    /// Registers the message handler and injects `convos.js`. Call before
    /// loading page content. The handler is registered through a weak proxy, so
    /// the bridge is not retained by the web view; keep a strong reference to it
    /// yourself.
    public func attach(to webView: WKWebView) {
        let controller = webView.configuration.userContentController
        // Install `window.convos` before any page script runs, so pages can use
        // the bridge from their first inline `<script>` without loading it.
        if let script = Self.bootstrapUserScript {
            controller.addUserScript(script)
        }
        controller.add(
            WeakScriptMessageHandler(self),
            name: Self.messageHandlerName
        )
        self.webView = webView
    }

    /// The `window.convos` bootstrap (`convos.js`) as a document-start user
    /// script. `attach(to:)` installs it for the common case. A host that
    /// clears and reinstalls its own user scripts on every load - a pooled web
    /// view, say - must reinstall this one alongside them, or `window.convos`
    /// survives only until the next clear. Nil only if the bundled `convos.js`
    /// resource is missing.
    public static var bootstrapUserScript: WKUserScript? {
        guard let source = Self.convosScriptSource else { return nil }
        return WKUserScript(
            source: source,
            injectionTime: .atDocumentStart,
            forMainFrameOnly: true
        )
    }

    /// The generated `convos.js`, bundled as a resource of this package.
    private static let convosScriptSource: String? = {
        guard let url = Bundle.module.url(forResource: "convos", withExtension: "js"),
              let source = try? String(contentsOf: url, encoding: .utf8) else {
            assertionFailure("convos.js missing from the ConvosBridge bundle")
            return nil
        }
        return source
    }()

    public func detach(from webView: WKWebView) {
        webView.configuration.userContentController.removeScriptMessageHandler(
            forName: Self.messageHandlerName
        )
        if self.webView === webView {
            self.webView = nil
        }
    }

    public func userContentController(
        _ userContentController: WKUserContentController,
        didReceive message: WKScriptMessage
    ) {
        guard message.name == Self.messageHandlerName, let raw = message.body as? String else { return }
        Task { [weak self] in
            guard let self, let response = await self.dispatcher.dispatch(raw) else { return }
            let script = "window.convos._handleResponse(JSON.parse(\(jsStringLiteral(response))))"
            await MainActor.run { [weak self] in
                self?.webView?.evaluateJavaScript(script, completionHandler: nil)
            }
        }
    }
}

/// `WKUserContentController` retains its message handlers; this proxy breaks
/// the resulting retain cycle with the web view configuration.
private final class WeakScriptMessageHandler: NSObject, WKScriptMessageHandler {
    private weak var target: WKScriptMessageHandler?

    init(_ target: WKScriptMessageHandler) {
        self.target = target
    }

    func userContentController(
        _ userContentController: WKUserContentController,
        didReceive message: WKScriptMessage
    ) {
        target?.userContentController(userContentController, didReceive: message)
    }
}
#endif
