import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version libs.versions.kotlin
    kotlin("plugin.serialization") version libs.versions.kotlin

    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.git)
    alias(libs.plugins.machete)
}

val gitVersion: groovy.lang.Closure<String> by extra

val modVersion = gitVersion().let { if (it.first() == 'v') it.drop(1) else it }
val mcVersion = stonecutter.current.version
val mcTarget = property("minecraft.target")!!
val fabricYarnBuild = property("fabric.yarn_build")!!
val fabricVersion = "${property("fabric.api")}+${stonecutter.current.project}"
val modmenuVersion = property("api.modmenu")!!
val yaclVersion = property("api.yacl")!!
val javaVersion = "${property("java.version")}".toInt(10)

version = "$modVersion+$mcVersion"
group = "dev.rvbsm"
base.archivesName = rootProject.name

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

    mixin {
        useLegacyMixinAp = false
    }
}

machete {
    json.enabled = false
}

repositories {
    mavenCentral()
    maven("https://maven.terraformersmc.com/releases")
    maven("https://maven.isxander.dev/releases")
    maven("https://maven.isxander.dev/snapshots")
    maven("https://maven.quiltmc.org/repository/release")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings("net.fabricmc:yarn:$mcVersion+build.$fabricYarnBuild:v2")

    modImplementation(libs.bundles.fabric)
    setOf(
        "fabric-api-base",
        "fabric-command-api-v2",
        "fabric-key-binding-api-v1",
        "fabric-lifecycle-events-v1",
        "fabric-networking-api-v1"
    ).map { fabricApi.module(it, fabricVersion) }.forEach(::modImplementation)

    // fabric-resource-loader-v0: @Mixin target net.minecraft.class_60 was not found
    modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:$fabricVersion")

    modApi("com.terraformersmc:modmenu:$modmenuVersion")

    modApi("dev.isxander.yacl:yet-another-config-lib-fabric:$yaclVersion") {
        exclude("net.fabricmc.fabric-api", "fabric-api")
    }

    implementation(libs.bundles.kaml)
    include(libs.bundles.kaml)
}

tasks {
    processResources {
        inputs.property("version", "$version")
        inputs.property("mcPredicate", "$mcTarget")

        filesMatching("fabric.mod.json") {
            expand("version" to version, "mcPredicate" to mcTarget)
        }
    }

    jar {
        from("LICENSE")
    }

    withType(JavaCompile::class.java).configureEach {
        options.release = javaVersion
    }

    withType(KotlinCompile::class.java).configureEach {
        kotlinOptions.jvmTarget = "$javaVersion"
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

    sourceCompatibility = enumValues<JavaVersion>()[javaVersion - 1]
    targetCompatibility = enumValues<JavaVersion>()[javaVersion - 1]
}

kotlin {
    jvmToolchain(javaVersion)
}
