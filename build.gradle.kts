import java.io.ByteArrayOutputStream

plugins {
	java
	id("fabric-loom")
	id("com.github.johnrengelman.shadow")
}

allprojects {
	apply(plugin = "java")
	apply(plugin = "fabric-loom")

	group = property("maven_group")!!
	version = "git --no-pager describe --tags --always".runCommand()

	dependencies {
		minecraft(rootProject.libs.minecraft)
		mappings("net.fabricmc:yarn:${rootProject.libs.versions.yarn.mappings.get()}:v2")

		modImplementation(rootProject.libs.fabric.loader)
	}

	loom {
		runs["server"].ideConfigGenerated(project.rootProject == project)
		runs["client"].ideConfigGenerated(project.rootProject == project)
	}

	tasks {
		processResources {
			inputs.property("version", project.version)
			filesMatching("fabric.mod.json") {
				expand("version" to project.version)
			}
		}

		jar {
			from(rootProject.file("LICENSE"))
		}

		compileJava {
			options.encoding = Charsets.UTF_8.name()
			options.release.set(17)
		}
	}

	java {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
}

subprojects {
	tasks.remapJar {
		destinationDirectory.set(rootProject.tasks.remapJar.get().destinationDirectory)
	}
}

repositories {
	maven("https://maven.terraformersmc.com/")
	maven("https://maven.shedaniel.me/")
}

configurations {
	modApi {
		extendsFrom(shadow.get())
	}
}

dependencies {
	include(fabricApi.module("fabric-command-api-v2", libs.versions.fabric.api.get()))
	include(fabricApi.module("fabric-events-interaction-v0", libs.versions.fabric.api.get()))
	include(fabricApi.module("fabric-networking-api-v1", rootProject.libs.versions.fabric.api.get()))

	include(project(":fsit-client", configuration = "namedElements"))

	modApi(libs.modmenu)
	modApi(libs.clothconfig)

	shadow(libs.nightconfig.toml)
}

tasks {
	shadowJar {
		dependsOn(jar)
		configurations = listOf(project.configurations.shadow.get())
	}

	remapJar {
		dependsOn(shadowJar)
		inputFile.set(shadowJar.get().archiveFile)
	}
}

java {
	withSourcesJar()
}

fun String.runCommand(currentWorkingDir: File = file("./")): String {
	val byteOut = ByteArrayOutputStream()
	project.exec {
		workingDir = currentWorkingDir
		commandLine = this@runCommand.split("\\s".toRegex())
		standardOutput = byteOut
	}
	return String(byteOut.toByteArray()).trim()
}
