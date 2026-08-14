import ConvosBridge
import UIKit
import WebKit

final class WebViewController: UIViewController {

    private var webView: WKWebView!
    // The bridge registers through a weak proxy, so it must be retained here.
    private var bridge: ConvosWebBridge!

    override func viewDidLoad() {
        super.viewDidLoad()

        webView = WKWebView(frame: .zero, configuration: WKWebViewConfiguration())
        bridge = ConvosWebBridge(plugins: [
            EventsPluginDispatcher(DemoEvents()),
        ])
        bridge.onReady = { print("convos.js reported ready") }
        bridge.attach(to: webView)

        view = webView

        guard let indexURL = Bundle.main.url(forResource: "index", withExtension: "html") else {
            assertionFailure("index.html missing from the app bundle")
            return
        }
        webView.loadFileURL(indexURL, allowingReadAccessTo: Bundle.main.resourceURL ?? indexURL)
    }
}
