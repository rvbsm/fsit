rootProject.name = "fsit"

private val minecraftTargets = arrayOf(
    "1.20" to "1.20.1",
//    "1.20.2" to "1.20.4",
//    "1.20.5" to "1.20.6",
    "1.21" to "1.21.1",
    "1.21.2" to "1.21.5-rc2",
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net")
        maven("https://maven.kikugie.dev/releases")
        //maven("https://maven.kikugie.dev/snapshots")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.5.1"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"
    shared {
        minecraftTargets.forEach {
            vers(it.first, it.second)
        }
    }
    create(rootProject)
}
