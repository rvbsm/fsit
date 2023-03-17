import java.io.ByteArrayOutputStream

plugins {
	java
	alias(libs.plugins.fabric.loom)
}

group = property("maven_group")!!
version = "git --no-pager describe --tags --always".runCommand()

repositories {
	maven("https://maven.terraformersmc.com/")
}

dependencies {
	minecraft(libs.minecraft)
	mappings("net.fabricmc:yarn:${libs.versions.yarn.mappings.get()}:v2")

	modImplementation(libs.fabric.loader)
	modApi(libs.modmenu)

	implementation(libs.toml4j)
	include(libs.toml4j)
}

tasks {
	processResources {
		inputs.property("version", version)
		filesMatching("fabric.mod.json") {
			expand(mutableMapOf("version" to project.version))
		}
	}

	jar {
		from("LICENSE")
	}

	compileJava {
		options.encoding = Charsets.UTF_8.name()
		options.release.set(17)
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
