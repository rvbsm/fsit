import org.codehaus.groovy.runtime.ProcessGroovyMethods

plugins {
    kotlin("jvm") version libs.versions.kotlin
    kotlin("plugin.serialization") version libs.versions.kotlin

    alias(libs.plugins.fabric.loom)
}

val gitVersion = "git describe --tags".execute().text!!.drop(1).trim()
val mcVersion = stonecutter.current.version
val mcPredicate = property("version_predicate")
val yarnBuild = property("fabric.yarn_build")
val fabricVersion = property("fabric.api")
val modmenuVersion = property("api.modmenu")
val yaclVersion = property("api.yacl")

group = "dev.rvbsm"
version = "$gitVersion+$mcVersion"
base { archivesName = rootProject.name }

repositories {
    maven("https://maven.terraformersmc.com/releases")
    maven("https://maven.isxander.dev/releases")
    mavenCentral()
}

loom {
    accessWidenerPath = rootProject.file("src/main/resources/fsit.accesswidener")

    splitEnvironmentSourceSets()
    mods.register(name) {
        sourceSet("main")
        sourceSet("client")
    }

    runConfigs.all {
        ideConfigGenerated(true)
        runDir = "../../run"
    }
}

val transitiveInclude: Configuration by configurations.creating {
    exclude("com.mojang")
    exclude("org.jetbrains.kotlin")
    exclude("org.jetbrains.kotlinx")
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings("net.fabricmc:yarn:$mcVersion+build.$yarnBuild:v2")

    modImplementation(rootProject.libs.bundles.fabric)
    setOf(
        "fabric-api-base",
        "fabric-command-api-v1",
        "fabric-events-interaction-v0",
        "fabric-key-binding-api-v1",
        "fabric-lifecycle-events-v1",
        "fabric-networking-api-v1"
    ).map { fabricApi.module(it, "$fabricVersion+$mcVersion") }.forEach(::modImplementation)

    modApi("com.terraformersmc:modmenu:$modmenuVersion")

    // Could not find com.twelvemonkeys.imageio:imageio-core:3.10.0-SNAPSHOT.
    modApi(rootProject.libs.bundles.twelvemonkeys.imageio)
    modApi("dev.isxander.yacl:yet-another-config-lib-fabric:$yaclVersion")

    implementation(rootProject.libs.kaml)
    transitiveInclude(rootProject.libs.kaml)

    transitiveInclude.incoming.artifacts.forEach {
        include("${it.id.componentIdentifier}")
    }
}

tasks {
    processResources {
        inputs.property("version", "$version")
        inputs.property("mcPredicate", "$mcPredicate")

        filesMatching("fabric.mod.json") {
            expand("version" to version, "mcPredicate" to mcPredicate)
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
