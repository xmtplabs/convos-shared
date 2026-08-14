import ConvosBridge
import Foundation

/// Stub implementations backing the demo page.
final class DemoEvents: EventsPlugin {
    func trackEvent(name: String) {
        print("trackEvent: \(name)")
    }
}
