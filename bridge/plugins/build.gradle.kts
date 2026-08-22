plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.ksp)
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(kotlin("stdlib"))
    api(project(":annotations"))
    api(project(":runtime"))
    ksp(project(":codegen"))

    testImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
}

val repoRoot: java.io.File = rootProject.projectDir.parentFile

val generatedSwiftDir = layout.buildDirectory.dir("generated/ksp/main/resources/swift")
val generatedConvosJs = layout.buildDirectory.file("generated/ksp/main/resources/web/convos.js")
val swiftPackageOutDir = repoRoot.resolve("ConvosBridge")
val convosJsOut = repoRoot.resolve("web/convos.js")

val syncBridgeSwiftPackage = tasks.register<Sync>("syncBridgeSwiftPackage") {
    group = "convos"
    description = "Mirrors the KSP-generated Swift sources into <repoRoot>/ConvosBridge."
    dependsOn("kspKotlin")
    from(generatedSwiftDir)
    into(swiftPackageOutDir)
}

val syncConvosJs = tasks.register("syncConvosJs") {
    group = "convos"
    description = "Copies the generated convos.js to <repoRoot>/web/convos.js."
    dependsOn("kspKotlin")
    val jsProvider = generatedConvosJs
    val jsOut = convosJsOut
    inputs.file(jsProvider)
    outputs.file(jsOut)
    doLast {
        jsOut.parentFile.mkdirs()
        jsOut.writeText(jsProvider.get().asFile.readText())
    }
}

tasks.register("syncGeneratedArtifacts") {
    group = "convos"
    description = "Runs all generated-artifact sync steps (Swift package + convos.js)."
    dependsOn(syncBridgeSwiftPackage, syncConvosJs)
}

tasks.named("build") {
    dependsOn("syncGeneratedArtifacts")
}
