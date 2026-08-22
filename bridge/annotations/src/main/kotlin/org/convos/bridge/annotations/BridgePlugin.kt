package org.convos.bridge.annotations

/**
 * Marks a Kotlin interface as a bridge plugin: the single source of truth for a
 * set of methods exposed to JavaScript through the Convos web bridge.
 *
 * For every annotated interface the codegen processor emits:
 *  - a Kotlin [PluginHandler] that routes JSON envelopes to an implementation,
 *  - a Swift protocol + dispatcher inside the ConvosBridge Swift package,
 *  - the matching async methods on the generated `convos.js` `Convos` class.
 *
 * Rules enforced by the processor:
 *  - the interface name must end in `Plugin`; the wire plugin name is the
 *    lowerCamel simple name minus that suffix (`MembershipPlugin` -> `membership`),
 *  - `suspend` functions are request/response; non-suspend functions must return
 *    `Unit` and become fire-and-forget notifications,
 *  - parameter and return types must be primitives, enums, `@Serializable`
 *    classes, `List<T>` of those, or nullable variants.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class BridgePlugin
