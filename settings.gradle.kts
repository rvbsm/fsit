import dev.kikugie.stonecutter.gradle.StonecutterSettings

rootProject.name = "fsit"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.3.2"
}

extensions.configure<StonecutterSettings> {
    kotlinController(true)
    centralScript("build.gradle.kts")
    shared {
        versions("1.20.1", "1.20.2", "1.20.5")
    }
    create(rootProject)
}

