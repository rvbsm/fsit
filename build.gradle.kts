plugins {
    kotlin("jvm") version libs.versions.kotlin
    kotlin("plugin.serialization") version libs.versions.kotlin

    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.git)
}

val gitVersion: groovy.lang.Closure<String> by extra

val modVersion = gitVersion().let { if (it.first() == 'v') it.drop(1) else it }
val mcVersion = stonecutter.current.version
val mcPredicate = property("version_predicate")
val yarnBuild = property("fabric.yarn_build")
val fabricVersion = property("fabric.api")
val modmenuVersion = property("api.modmenu")
val yaclVersion = property("api.yacl")

version = "$modVersion+$mcVersion"
group = "dev.rvbsm"
base { archivesName = rootProject.name }

repositories {
    mavenCentral()
    maven("https://maven.terraformersmc.com/releases")
    maven("https://maven.isxander.dev/releases")
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

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings("net.fabricmc:yarn:$mcVersion+build.$yarnBuild:v2")

    modImplementation(libs.bundles.fabric)
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
    modApi(libs.bundles.twelvemonkeys.imageio)
    modApi("dev.isxander.yacl:yet-another-config-lib-fabric:$yaclVersion")

    implementation(libs.bundles.kaml)
    include(libs.bundles.kaml)
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

    if (stonecutter.current.isActive) {
        register("buildActive") {
            group = "project"
            dependsOn(build)
        }
    }
}

java {
    withSourcesJar()

    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}
