pluginManagement {
	repositories {
		maven("https://maven.fabricmc.net/")
		gradlePluginPortal()
		mavenCentral()
	}
	plugins {
		id("fabric-loom") version "1.2-SNAPSHOT"
		id("com.github.johnrengelman.shadow") version "8.1.1"
	}
}
rootProject.name = "fsit"
