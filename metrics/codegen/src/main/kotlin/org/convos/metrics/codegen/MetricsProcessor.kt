package org.convos.metrics.codegen

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import org.convos.metrics.codegen.kotlin.KotlinGenerator
import org.convos.metrics.codegen.navigation.NavigationExtractor
import org.convos.metrics.codegen.navigation.NavigationGraphYamlWriter
import org.convos.metrics.codegen.swift.SwiftGenerator

class MetricsProcessor(
    private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {
    private val navigationExtractor = NavigationExtractor(environment.logger)
    private val yamlWriter = NavigationGraphYamlWriter(environment.codeGenerator)
    private val kotlinGenerator = KotlinGenerator(environment.codeGenerator)
    private val swiftGenerator = SwiftGenerator(environment.codeGenerator)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val navigationGraph = navigationExtractor.extract(resolver)

        if (navigationGraph.targets.isNotEmpty()) {
            yamlWriter.write(navigationGraph)
            kotlinGenerator.generateCollectors(navigationGraph)
            swiftGenerator.generate(navigationGraph)
        }

        return emptyList()
    }
}
