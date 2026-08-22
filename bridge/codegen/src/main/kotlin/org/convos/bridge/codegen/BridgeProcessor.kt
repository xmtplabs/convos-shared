package org.convos.bridge.codegen

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import org.convos.bridge.codegen.js.ConvosJsGenerator
import org.convos.bridge.codegen.kotlin.KotlinHandlerGenerator
import org.convos.bridge.codegen.model.BridgeExtractor
import org.convos.bridge.codegen.swift.SwiftBridgeGenerator

class BridgeProcessor(
    environment: SymbolProcessorEnvironment,
) : SymbolProcessor {
    private val logger = environment.logger
    private val kotlinGenerator = KotlinHandlerGenerator(environment.codeGenerator)
    private val swiftGenerator = SwiftBridgeGenerator(environment.codeGenerator)
    private val jsGenerator = ConvosJsGenerator(environment.codeGenerator)
    private val extractor = BridgeExtractor(logger)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val model = extractor.extract(resolver)
        if (model.isEmpty) {
            return emptyList()
        }

        kotlinGenerator.generate(model)
        swiftGenerator.generate(model)
        jsGenerator.generate(model)

        return emptyList()
    }
}
