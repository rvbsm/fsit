rootProject.name = "fsit"
pluginManagement {
	repositories {
		maven("https://maven.fabricmc.net/") {
			name = "Fabric"
		}
		gradlePluginPortal()
	}
	plugins {
		id("fabric-loom") version "1.1-SNAPSHOT"
		id("com.github.johnrengelman.shadow") version "8.1.0"
	}
}
