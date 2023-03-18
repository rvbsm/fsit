import java.io.ByteArrayOutputStream

plugins {
	id("fabric-loom")
	id("com.github.johnrengelman.shadow")
}

group = property("maven_group")!!
version = "git --no-pager describe --tags --always".runCommand()

repositories {
	maven("https://maven.terraformersmc.com/")
	maven("https://maven.shedaniel.me/")
}

dependencies {
	minecraft(libs.minecraft)
	mappings("net.fabricmc:yarn:${libs.versions.yarn.mappings.get()}:v2")

	modImplementation(libs.fabric.loader)
	modApi(libs.modmenu)
	modApi(libs.clothconfig)

	implementation(libs.nightconfig.toml)
	shadow(libs.nightconfig.toml)
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

	shadowJar {
		dependsOn(jar)
		configurations = listOf(project.configurations.shadow.get())
		exclude("META-INF/**")

		relocate("com.electronwill.night-config", "dev.rvbsm.shadow.com.electronwill.night-config")
	}

	remapJar {
		dependsOn(shadowJar)
		inputFile.set(shadowJar.get().archiveFile.get())
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
