import org.codehaus.groovy.runtime.ProcessGroovyMethods

plugins {
    kotlin("jvm") version libs.versions.kotlin
    kotlin("plugin.serialization") version libs.versions.kotlin

    alias(libs.plugins.fabric.loom)
}

group = "dev.rvbsm"
version = "git describe --tags".execute().text!! .drop(1).trim()

repositories {
    maven("https://maven.terraformersmc.com/releases")
    maven("https://maven.isxander.dev/releases")
    mavenCentral()
}

loom {
    splitEnvironmentSourceSets()
    accessWidenerPath = file("src/main/resources/fsit.accesswidener")

    mods.register(name) {
        sourceSet("main")
        sourceSet("client")
    }
}

val transitiveInclude: Configuration by configurations.creating {
    exclude("com.mojang")
    exclude("org.jetbrains.kotlin")
    exclude("org.jetbrains.kotlinx")
}

dependencies {
    minecraft(libs.minecraft)
    mappings("${libs.fabric.yarn.get()}:v2")

    modImplementation(libs.bundles.fabric)
    setOf(
        "fabric-api-base",
        "fabric-command-api-v1",
        "fabric-events-interaction-v0",
        "fabric-key-binding-api-v1",
        "fabric-lifecycle-events-v1",
        "fabric-networking-api-v1"
    ).map { fabricApi.module(it, libs.versions.fabric.api.get()) }.forEach(::modImplementation)

    modApi(libs.modmenu)
    modApi(libs.yacl)

    implementation(libs.kaml)
    transitiveInclude(libs.kaml)

    transitiveInclude.incoming.artifacts.forEach {
        include("${it.id.componentIdentifier}")
    }
}

tasks {
    processResources {
        inputs.property("version", "$version")
        filesMatching("fabric.mod.json") {
            expand("version" to version)
        }
    }

    jar {
        from("LICENSE")
    }
}

java {
    withSourcesJar()

    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
}

fun String.execute(): Process = ProcessGroovyMethods.execute(this)
val Process.text: String? get() = ProcessGroovyMethods.getText(this)
