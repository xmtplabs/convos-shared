pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "bridge"

include(":annotations")
include(":codegen")
include(":runtime")
include(":plugins")
include(":runtime-android")
include(":demo-android")
