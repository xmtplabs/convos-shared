package org.convos.metrics.descriptors.navigation

abstract class CollectorDelegate {
    open fun navigatedTo(source: String, target: String) {}
    open fun presented(source: String, target: String) {}
    open fun closed(screen: String, context: ScreenContext) {}
}
