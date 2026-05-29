package org.convos.metrics.codegen.markdown

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import org.convos.metrics.codegen.core.CoreMetricsModel
import org.convos.metrics.codegen.navigation.NavigationGraph
import org.convos.metrics.codegen.navigation.NavigationMethodType

class MetricsMarkdownWriter(private val codeGenerator: CodeGenerator) {

    fun write(graph: NavigationGraph, coreModel: CoreMetricsModel) {
        val enumValues: Map<String, List<String>> =
            graph.enumTypes.associate { it.name to it.values } +
                coreModel.enumTypes.associate { it.name to it.values.map { v -> v.name } }

        val content = buildString {
            appendLine("# Metrics")
            appendLine()
            appendCoreActions(coreModel, enumValues)
            appendUserProperties(coreModel)
            appendNavigators(graph, enumValues)
        }

        codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true),
            packageName = "",
            fileName = "metrics",
            extensionName = "md",
        ).bufferedWriter().use { it.write(content) }
    }

    private fun renderType(type: String, nullable: Boolean, enumValues: Map<String, List<String>>): String {
        val base = type + if (nullable) "?" else ""
        val values = enumValues[type] ?: return base
        return "$base { ${values.joinToString(", ")} }"
    }

    private fun StringBuilder.appendCoreActions(
        model: CoreMetricsModel,
        enumValues: Map<String, List<String>>,
    ) {
        appendLine("## Core Actions")
        appendLine()
        val actions = model.coreActions?.actions
        if (actions.isNullOrEmpty()) {
            appendLine("_None defined._")
            appendLine()
            return
        }
        appendLine("| Event | Function | Parameters |")
        appendLine("|-------|----------|------------|")
        for (action in actions) {
            val params = if (action.parameters.isEmpty()) {
                "_none_"
            } else {
                action.parameters.joinToString("<br>") { p ->
                    "`${p.snakeName}`: ${renderType(p.type, p.nullable, enumValues)}"
                }
            }
            appendLine("| `${action.eventName}` | `${action.name}` | $params |")
        }
        appendLine()
    }

    private fun StringBuilder.appendUserProperties(model: CoreMetricsModel) {
        appendLine("## User Properties")
        appendLine()
        val props = model.userProperties?.properties
        if (props.isNullOrEmpty()) {
            appendLine("_None defined._")
            appendLine()
            return
        }
        appendLine("| Key | Field | Type | Nullable |")
        appendLine("|-----|-------|------|----------|")
        for (p in props) {
            appendLine("| `${p.snakeName}` | `${p.name}` | ${p.type} | ${if (p.nullable) "yes" else "no"} |")
        }
        appendLine()
    }

    private fun StringBuilder.appendNavigators(
        graph: NavigationGraph,
        enumValues: Map<String, List<String>>,
    ) {
        appendLine("## Navigators")
        appendLine()
        if (graph.targets.isEmpty()) {
            appendLine("_None defined._")
            appendLine()
            return
        }
        appendLine("![Navigator graph](navigators.png)")
        appendLine()
        appendLine("Graph source: [`navigators.dot`](navigators.dot). `navigators.png` is re-rendered by the build when Graphviz is installed.")
        appendLine()
        appendLine("| Screen | Args | Outgoing |")
        appendLine("|--------|------|----------|")
        for (descriptor in graph.targets.values) {
            val args = if (descriptor.args.parameters.isEmpty()) {
                "_none_"
            } else {
                descriptor.args.parameters.joinToString("<br>") { p ->
                    "`${p.name}`: ${renderType(p.type, p.nullable, enumValues)}"
                }
            }
            val outgoing = if (descriptor.navigationMethods.isEmpty()) {
                "_leaf_"
            } else {
                descriptor.navigationMethods.joinToString("<br>") { m ->
                    val verb = when (m.methodType) {
                        NavigationMethodType.NAVIGATE_TO -> "navigateTo"
                        NavigationMethodType.PRESENT -> "present"
                        else -> m.methodType.name.lowercase()
                    }
                    val targetName = m.targetScreen.removeSuffix("Navigator")
                    "$verb → `$targetName`"
                }
            }
            appendLine("| `${descriptor.metricsName}` | $args | $outgoing |")
        }
        appendLine()
    }
}
