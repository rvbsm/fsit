rootProject.name = "fsit"

private val minecraftTargets = setOf(
//    "1.20" to "1.20.1",
//    "1.20.2" to "1.20.4",
//    "1.20.5" to "1.20.6",
    "1.21" to "1.21.1",
    "1.21.2" to "1.21.5",
    "1.21.6" to "1.21.7-rc2"
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net")
        //maven("https://maven.kikugie.dev/snapshots")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.6.1"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    create(rootProject) {
        versions(minecraftTargets)
    }
}
