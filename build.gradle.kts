val gitVersion: groovy.lang.Closure<String> by extra

plugins {
	alias(libs.plugins.fabric.loom)
	alias(libs.plugins.shadow)
	alias(libs.plugins.git)
}

group = "dev.rvbsm"
version = gitVersion()

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

val shadowImplementation: Configuration by configurations.creating {
	configurations.implementation.get().extendsFrom(this)
}

dependencies {
	minecraft(libs.minecraft)
	mappings("net.fabricmc:yarn:${libs.versions.yarn.mappings.get()}:v2")

	modImplementation(libs.fabric.loader)
//	modImplementation(libs.fabric.api)

	listOf("fabric-events-interaction-v0", "fabric-networking-api-v1", "fabric-command-api-v2").forEach {
		modInclude(fabricApi.module(it, libs.versions.fabric.api.get()))
	}

	shadowImplementation(libs.toml4j) {
		exclude("com.google.code.gson", "gson")
	}

	modApi(libs.modmenu)
	modApi(libs.clothconfig) {
		exclude(group = "net.fabricmc.fabric-api")
	}

	compileOnly(libs.lombok)
	annotationProcessor(libs.lombok)
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

	shadowJar {
		configurations = listOf(shadowImplementation)
		archiveClassifier.set("shadow")
		from(sourceSets["client"].output)
	}

	remapJar {
		dependsOn(shadowJar)
		inputFile.set(shadowJar.get().archiveFile)
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
