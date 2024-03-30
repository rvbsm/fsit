import dev.kikugie.stonecutter.gradle.StonecutterSettings

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases")
        mavenCentral()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.3.+"
}

extensions.configure<StonecutterSettings> {
    kotlinController = true
    centralScript = "build.gradle.kts"
    shared {
        versions("1.20.2", "1.20.1")
    }
    create(rootProject)
}

rootProject.name = "fsit"
