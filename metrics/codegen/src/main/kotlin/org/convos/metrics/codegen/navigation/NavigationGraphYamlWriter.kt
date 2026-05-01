package org.convos.metrics.codegen.navigation

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies

class NavigationGraphYamlWriter(private val codeGenerator: CodeGenerator) {

    private val yaml = Yaml(
        configuration = YamlConfiguration(
            encodeDefaults = true,
        ),
    )

    fun write(graph: NavigationGraph) {
        val content = yaml.encodeToString(NavigationGraph.serializer(), graph)

        codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true),
            packageName = "",
            fileName = "navigation-graph",
            extensionName = "yaml",
        ).bufferedWriter().use { it.write(content) }
    }
}
