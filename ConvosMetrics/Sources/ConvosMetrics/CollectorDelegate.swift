public struct ScreenContext {
    public let durationSecs: Float

    public init(durationSecs: Float) {
        self.durationSecs = durationSecs
    }
}

open class CollectorDelegate {
    public init() {}

    open func navigatedTo(source: String, target: String) {}
    open func presented(source: String, target: String) {}
    open func closed(screen: String, context: ScreenContext) {}

    open func identify(userId: String) {}
    open func updateUserProperties(properties: [String: Any?]) {}
    open func sendEvent(name: String, properties: [String: Any?]) {}
}
