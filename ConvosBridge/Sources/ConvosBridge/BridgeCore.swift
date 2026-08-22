// Static runtime for the ConvosBridge package. Copied verbatim into the
// generated Swift package by the bridge codegen processor; edit the copy in
// bridge/codegen/src/main/resources/swift/ instead.

import Foundation

/// Routes the methods of one plugin to its implementation. One dispatcher per
/// `@BridgePlugin` interface is generated alongside this file.
public protocol BridgePluginDispatcher {
    var pluginName: String { get }

    /// Invokes `method` and returns the JSON-compatible result value
    /// (`nil` for Unit). Throws `BridgeError` for routing/decoding failures.
    func handle(method: String, params: BridgeParams) async throws -> Any?

    /// Returns a stream of JSON-encodable payloads for the named event
    /// function, or `nil` if this plugin exposes no such event.
    func events(_ name: String) -> AsyncStream<Any>?
}

public extension BridgePluginDispatcher {
    func events(_ name: String) -> AsyncStream<Any>? { nil }
}

public enum BridgeError: Error {
    case invalidRequest(String)
    case methodNotFound(plugin: String, method: String)
    case invalidParams(String)
    case plugin(String)

    var code: String {
        switch self {
        case .invalidRequest: return "INVALID_REQUEST"
        case .methodNotFound: return "METHOD_NOT_FOUND"
        case .invalidParams: return "INVALID_PARAMS"
        case .plugin: return "PLUGIN_ERROR"
        }
    }

    var message: String {
        switch self {
        case .invalidRequest(let message): return message
        case .methodNotFound(let plugin, let method): return "Method not found: \(plugin).\(method)"
        case .invalidParams(let message): return message
        case .plugin(let message): return message
        }
    }
}

/// The `params` object of a request envelope, as produced by JSONSerialization.
public struct BridgeParams {
    private let values: [String: Any]

    public init(_ values: [String: Any]) {
        self.values = values
    }

    /// Decodes one named parameter. A missing key decodes as JSON null so
    /// nullable parameters may be omitted; a missing or mistyped value for a
    /// non-optional parameter throws `BridgeError.invalidParams`.
    public func decode<T: Decodable>(_ key: String) throws -> T {
        let raw = values[key] ?? NSNull()
        do {
            let data = try JSONSerialization.data(withJSONObject: ["v": raw])
            return try JSONDecoder().decode(Box<T>.self, from: data).v
        } catch {
            throw BridgeError.invalidParams("Invalid value for parameter '\(key)'")
        }
    }

    private struct Box<T: Decodable>: Decodable {
        let v: T
    }
}

public enum BridgeJSON {
    /// Converts an Encodable result into the JSONSerialization value placed at
    /// `result` in the response envelope (`NSNull` for nil).
    public static func encodeResult<T: Encodable>(_ value: T) throws -> Any {
        let data = try JSONEncoder().encode(Box(v: value))
        let object = try JSONSerialization.jsonObject(with: data)
        guard let dictionary = object as? [String: Any], let result = dictionary["v"] else {
            return NSNull()
        }
        return result
    }

    private struct Box<T: Encodable>: Encodable {
        let v: T
    }
}

/// Platform-independent core of the web bridge: parses a raw request envelope,
/// routes it to the matching plugin dispatcher, and renders the response
/// envelope. The Kotlin twin lives in `org.convos.bridge.runtime.BridgeDispatcher`.
public final class BridgeDispatcher {
    /// Reserved method the page sends from `window.convos.onReady()`.
    public static let readyMethod = "_bridge.ready"

    /// Reserved methods the page sends when (un)subscribing to an event.
    public static let subscribeMethod = "_bridge.subscribe"
    public static let unsubscribeMethod = "_bridge.unsubscribe"

    private let dispatchersByPlugin: [String: BridgePluginDispatcher]
    private let onReady: () -> Void
    private let emitEvent: ((String) -> Void)?

    /// Active event collectors, keyed by full wire event name (`plugin.function`).
    private var subscriptions: [String: Task<Void, Never>] = [:]
    private let subscriptionsLock = NSLock()

    public init(
        plugins: [BridgePluginDispatcher],
        onReady: @escaping () -> Void = {},
        emitEvent: ((String) -> Void)? = nil
    ) {
        var byPlugin: [String: BridgePluginDispatcher] = [:]
        for plugin in plugins {
            byPlugin[plugin.pluginName] = plugin
        }
        self.dispatchersByPlugin = byPlugin
        self.onReady = onReady
        self.emitEvent = emitEvent
    }

    /// Dispatches one raw request. Returns the JSON response envelope to
    /// deliver back to the page, or nil when no response should be sent
    /// (notifications and requests whose id cannot be recovered).
    public func dispatch(_ rawJson: String) async -> String? {
        guard
            let data = rawJson.data(using: .utf8),
            let parsed = try? JSONSerialization.jsonObject(with: data),
            let envelope = parsed as? [String: Any],
            let method = envelope["method"] as? String
        else {
            guard let id = bestEffortId(rawJson) else { return nil }
            return response(id: id, error: .invalidRequest("Unparseable request"))
        }

        let id = envelope["id"] as? String
        let params = BridgeParams(envelope["params"] as? [String: Any] ?? [:])

        do {
            let result = try await invoke(method: method, params: params)
            guard let id else { return nil }
            return response(id: id, result: result ?? NSNull())
        } catch {
            guard let id else { return nil }
            let bridgeError = error as? BridgeError ?? .plugin("\(error)")
            return response(id: id, error: bridgeError)
        }
    }

    private func invoke(method: String, params: BridgeParams) async throws -> Any? {
        if method == Self.readyMethod {
            onReady()
            return nil
        }
        if method == Self.subscribeMethod {
            subscribe(params)
            return nil
        }
        if method == Self.unsubscribeMethod {
            unsubscribe(params)
            return nil
        }
        let parts = method.split(separator: ".", maxSplits: 1)
        guard parts.count == 2 else {
            throw BridgeError.methodNotFound(plugin: "?", method: method)
        }
        let pluginName = String(parts[0])
        let functionName = String(parts[1])
        guard let dispatcher = dispatchersByPlugin[pluginName] else {
            throw BridgeError.methodNotFound(plugin: pluginName, method: functionName)
        }
        return try await dispatcher.handle(method: functionName, params: params)
    }

    /// Starts collecting the requested event stream, pushing each emission to
    /// the page via `emitEvent`. No-op if events are unsupported (no sink) or
    /// the event is already subscribed / unknown.
    private func subscribe(_ params: BridgeParams) {
        guard let emitEvent, let event: String = try? params.decode("event") else { return }
        let parts = event.split(separator: ".", maxSplits: 1)
        guard parts.count == 2,
              let dispatcher = dispatchersByPlugin[String(parts[0])],
              let stream = dispatcher.events(String(parts[1])) else { return }

        subscriptionsLock.lock()
        defer { subscriptionsLock.unlock() }
        if subscriptions[event] != nil { return }
        // The task captures only locals (event, stream, emitEvent), never self.
        subscriptions[event] = Task {
            for await payload in stream {
                let envelope: [String: Any] = ["event": event, "params": payload]
                if let data = try? JSONSerialization.data(withJSONObject: envelope),
                   let json = String(data: data, encoding: .utf8) {
                    emitEvent(json)
                }
            }
        }
    }

    private func unsubscribe(_ params: BridgeParams) {
        guard let event: String = try? params.decode("event") else { return }
        subscriptionsLock.lock()
        let task = subscriptions.removeValue(forKey: event)
        subscriptionsLock.unlock()
        task?.cancel()
    }

    private func response(id: String, result: Any) -> String? {
        serialize(["id": id, "ok": true, "result": result])
    }

    private func response(id: String, error: BridgeError) -> String? {
        serialize(["id": id, "ok": false, "error": ["code": error.code, "message": error.message]])
    }

    private func serialize(_ envelope: [String: Any]) -> String? {
        guard let data = try? JSONSerialization.data(withJSONObject: envelope) else { return nil }
        return String(data: data, encoding: .utf8)
    }

    private func bestEffortId(_ rawJson: String) -> String? {
        guard
            let data = rawJson.data(using: .utf8),
            let parsed = try? JSONSerialization.jsonObject(with: data),
            let envelope = parsed as? [String: Any]
        else { return nil }
        return envelope["id"] as? String
    }
}

/// Renders a string as a double-quoted JavaScript string literal, escaping
/// everything that could break out of the literal — including U+2028/U+2029,
/// which are valid in JSON strings but are line terminators in JavaScript.
func jsStringLiteral(_ value: String) -> String {
    var literal = "\""
    for scalar in value.unicodeScalars {
        switch scalar {
        case "\\": literal += "\\\\"
        case "\"": literal += "\\\""
        case "\n": literal += "\\n"
        case "\r": literal += "\\r"
        case "\u{2028}": literal += "\\u2028"
        case "\u{2029}": literal += "\\u2029"
        default:
            if scalar.value < 0x20 {
                literal += String(format: "\\u%04x", scalar.value)
            } else {
                literal.unicodeScalars.append(scalar)
            }
        }
    }
    return literal + "\""
}
