package org.convos.metrics.codegen.graphviz

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import org.convos.metrics.codegen.navigation.NavigationGraph
import org.convos.metrics.codegen.navigation.NavigationMethodType

class NavigatorGraphRenderer(private val codeGenerator: CodeGenerator) {

    fun render(graph: NavigationGraph) {
        if (graph.targets.isEmpty()) return

        val depths = computeDepths(graph)
        val nodesByDepth = graph.targets.values
            .groupBy { depths[it.name] ?: 0 }
            .toSortedMap()

        val dot = buildString {
            appendLine("digraph navigators {")
            appendLine("    rankdir=LR;")
            appendLine("    node [shape=rectangle, style=rounded, fontname=\"Helvetica\"];")
            appendLine("    edge [fontname=\"Helvetica\", fontsize=10];")
            appendLine()

            for ((depth, descriptors) in nodesByDepth) {
                if (depth <= 1) {
                    appendLine("    { rank=same;")
                    for (descriptor in descriptors) {
                        appendLine("        \"${descriptor.metricsName}\";")
                    }
                    appendLine("    }")
                } else {
                    for (descriptor in descriptors) {
                        appendLine("    \"${descriptor.metricsName}\";")
                    }
                }
            }
            appendLine()

            for (descriptor in graph.targets.values) {
                for (method in descriptor.navigationMethods) {
                    val target = graph.targets[method.targetScreen] ?: continue
                    val attrs = when (method.methodType) {
                        NavigationMethodType.NAVIGATE_TO -> "[color=black]"
                        NavigationMethodType.PRESENT -> "[color=blue, style=dashed]"
                        NavigationMethodType.CLOSED -> "[color=gray]"
                    }
                    appendLine("    \"${descriptor.metricsName}\" -> \"${target.metricsName}\" $attrs;")
                }
            }

            appendLine("}")
        }

        codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true),
            packageName = "",
            fileName = "navigators",
            extensionName = "dot",
        ).bufferedWriter().use { it.write(dot) }
    }

    private fun computeDepths(graph: NavigationGraph): Map<String, Int> {
        val rootName = graph.targets.values
            .firstOrNull { it.metricsName == TAB_ROOT_METRICS_NAME }
            ?.name
            ?: return emptyMap()

        val depths = mutableMapOf<String, Int>()
        val queue: ArrayDeque<String> = ArrayDeque()
        depths[rootName] = 0
        queue.add(rootName)
        while (queue.isNotEmpty()) {
            val currentName = queue.removeFirst()
            val current = graph.targets[currentName] ?: continue
            val currentDepth = depths.getValue(currentName)
            for (method in current.navigationMethods) {
                val targetName = method.targetScreen
                if (graph.targets.containsKey(targetName) && targetName !in depths) {
                    depths[targetName] = currentDepth + 1
                    queue.add(targetName)
                }
            }
        }

        val unreachableDepth = (depths.values.maxOrNull() ?: 0) + 1
        for (name in graph.targets.keys) {
            depths.putIfAbsent(name, unreachableDepth)
        }

        return depths
    }

    companion object {
        private const val TAB_ROOT_METRICS_NAME = "tab_root"
    }
}
