plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.ksp)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(kotlin("stdlib"))
    api(project(":annotations"))
    ksp(project(":codegen"))
}

val repoRoot: java.io.File = rootProject.projectDir.parentFile

val generatedSwiftDir = layout.buildDirectory.dir("generated/ksp/main/resources/swift")
val generatedManifestFile = layout.buildDirectory.file("generated/ksp/main/resources/swiftpackage/Package.swift")
val generatedMetricsMd = layout.buildDirectory.file("generated/ksp/main/resources/metrics.md")
val swiftPackageOutDir = repoRoot.resolve("ConvosMetrics")
val readmeFile = repoRoot.resolve("README.md")

val syncSwiftPackage = tasks.register<Sync>("syncSwiftPackage") {
    group = "convos"
    description = "Mirrors the KSP-generated Swift sources into <repoRoot>/ConvosMetrics."
    dependsOn("kspKotlin")
    from(generatedSwiftDir)
    into(swiftPackageOutDir)
}

val syncSwiftManifest = tasks.register("syncSwiftManifest") {
    group = "convos"
    description = "Copies the KSP-generated Package.swift manifest to <repoRoot>/Package.swift."
    dependsOn("kspKotlin")
    val manifestProvider = generatedManifestFile
    val manifestOut = repoRoot.resolve("Package.swift")
    inputs.file(manifestProvider)
    outputs.file(manifestOut)
    doLast {
        manifestOut.writeText(manifestProvider.get().asFile.readText())
    }
}

val syncReadmeMetrics = tasks.register("syncReadmeMetrics") {
    group = "convos"
    description = "Splices the generated metrics.md between AUTOGEN markers in <repoRoot>/README.md."
    dependsOn("kspKotlin")
    val generatedMdProvider = generatedMetricsMd
    val readmeFileLocal = readmeFile
    inputs.file(generatedMdProvider)
    inputs.file(readmeFileLocal)
    outputs.file(readmeFileLocal)
    doLast {
        val startMarker = "<!-- AUTOGEN:METRICS START -->"
        val endMarker = "<!-- AUTOGEN:METRICS END -->"
        check(readmeFileLocal.exists()) {
            "Expected README.md at ${readmeFileLocal.absolutePath}. Create one containing the $startMarker / $endMarker markers."
        }
        val original = readmeFileLocal.readText()
        val startIdx = original.indexOf(startMarker)
        val endIdx = original.indexOf(endMarker)
        check(startIdx >= 0 && endIdx > startIdx) {
            "README.md is missing the AUTOGEN markers. Add `$startMarker` and `$endMarker` on their own lines."
        }
        val generated = generatedMdProvider.get().asFile.readText().trimEnd()
        val before = original.substring(0, startIdx + startMarker.length)
        val after = original.substring(endIdx)
        val updated = buildString {
            append(before)
            append('\n')
            append('\n')
            append(generated)
            append('\n')
            append('\n')
            append(after)
        }
        if (updated != original) {
            readmeFileLocal.writeText(updated)
        }
    }
}

tasks.register("syncGeneratedArtifacts") {
    group = "convos"
    description = "Runs all generated-artifact sync steps (Swift package + README)."
    dependsOn(syncSwiftPackage, syncSwiftManifest, syncReadmeMetrics)
}

tasks.named("build") {
    dependsOn("syncGeneratedArtifacts")
}
