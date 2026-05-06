package org.convos.metrics.codegen.graphviz

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import org.convos.metrics.codegen.navigation.NavigationGraph
import org.convos.metrics.codegen.navigation.NavigationMethodType

class NavigatorGraphRenderer(private val codeGenerator: CodeGenerator) {

    fun render(graph: NavigationGraph) {
        if (graph.targets.isEmpty()) return

        val dot = buildString {
            appendLine("digraph navigators {")
            appendLine("    rankdir=LR;")
            appendLine("    node [shape=rectangle, style=rounded, fontname=\"Helvetica\"];")
            appendLine("    edge [fontname=\"Helvetica\", fontsize=10];")
            appendLine()

            for (descriptor in graph.targets.values) {
                appendLine("    \"${descriptor.metricsName}\";")
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
}
