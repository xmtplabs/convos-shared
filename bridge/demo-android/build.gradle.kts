plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "org.convos.bridge.demo"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.convos.bridge.demo"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    sourceSets["main"].assets.srcDir(layout.buildDirectory.dir("demoAssets"))
}

dependencies {
    implementation(project(":plugins"))
    implementation(project(":runtime-android"))
    implementation(libs.kotlinx.coroutines.android)
}

val syncDemoAssets = tasks.register<Sync>("syncDemoAssets") {
    group = "convos"
    description = "Copies the shared demo page into the app assets."
    // convos.js is injected by the runtime and ships as a runtime-android asset,
    // so the page no longer needs its own copy.
    from(rootProject.layout.projectDirectory.file("demo-shared/index.html"))
    into(layout.buildDirectory.dir("demoAssets"))
}

tasks.named("preBuild") {
    dependsOn(syncDemoAssets)
}
