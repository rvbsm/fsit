import java.io.ByteArrayOutputStream

plugins {
	alias(libs.plugins.fabric.loom)
}

group = property("maven_group")!!
version = "git --no-pager describe --tags --always".runCommand()

repositories {
	maven("https://maven.terraformersmc.com/")
	maven("https://maven.shedaniel.me/")
}

loom {
	splitEnvironmentSourceSets()

	mods.register("fsit") {
		sourceSet(sourceSets["main"])
		sourceSet(sourceSets["client"])
	}
}

val modInclude: Configuration by configurations.creating {
	configurations.modImplementation.get().extendsFrom(this)
	configurations.include.get().extendsFrom(this)
}

dependencies {
	minecraft(libs.minecraft)
	mappings("net.fabricmc:yarn:${libs.versions.yarn.mappings.get()}:v2")

	modImplementation(libs.fabric.loader)
//	modImplementation(libs.fabric.api)

	listOf("fabric-events-interaction-v0", "fabric-networking-api-v1", "fabric-command-api-v2").forEach {
		modInclude(fabricApi.module(it, libs.versions.fabric.api.get()))
	}

	modInclude(libs.nightconfig.core)
	modInclude(libs.nightconfig.toml)

	modApi(libs.modmenu)
	modApi(libs.clothconfig)
}

tasks {
	compileJava {
		options.encoding = Charsets.UTF_8.name()
		options.release.set(17)
	}

	processResources {
		inputs.property("version", project.version)
		filesMatching("fabric.mod.json") {
			expand("version" to project.version)
		}
	}

	jar {
		from(file("LICENSE"))
	}
}

java {
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
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
