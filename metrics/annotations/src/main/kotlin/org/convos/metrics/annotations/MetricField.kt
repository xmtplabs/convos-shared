package org.convos.metrics.annotations

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class MetricField(
    val name: String = "",
)
