import com.palantir.gradle.gitversion.VersionDetails

val gitVersion: groovy.lang.Closure<String> by extra
val versionDetails: groovy.lang.Closure<VersionDetails> by extra

val gitDetails = versionDetails()

plugins {
    kotlin("jvm") version libs.versions.kotlin
    kotlin("plugin.serialization") version libs.versions.kotlin

    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.publish)
    alias(libs.plugins.git)
    alias(libs.plugins.machete)
}

private class ModMetadata {
    val modVersion = gitDetails.lastTag.dropFirstIf('v')
    val minecraftVersion = stonecutter.current.version

    val javaVersion = "${property("java.version")}".toInt(10)

    val minecraftTargetStart = "${property("minecraft.target.start")}"
    val minecraftTargetEnd = "${property("minecraft.target.end")}"
    val minecraftTarget =
        ">=$minecraftTargetStart-" + (" <=$minecraftTargetEnd".takeUnless { minecraftTargetEnd == "latest" } ?: "")

    val modrinthId = "${property("mod.modrinth_id")}"
}

private class ModLibraries(metadata: ModMetadata) {
    private val fabricYarnBuild = "${property("fabric.yarn_build")}"
    val fabricApiVersion = "${property("fabric.api")}+${metadata.minecraftVersion}"
    private val modmenuVersion = "${property("api.modmenu")}"
    private val yaclVersion = "${property("api.yacl")}"

    val minecraft = "com.mojang:minecraft:${metadata.minecraftVersion}"
    val fabricYarn = "net.fabricmc:yarn:${metadata.minecraftVersion}+build.$fabricYarnBuild:v2"
    val fabricApiModules = setOf(
        "fabric-api-base",
        "fabric-command-api-v2",
        "fabric-key-binding-api-v1",
        "fabric-lifecycle-events-v1",
        "fabric-networking-api-v1",
    )
    val fabricApi by lazy {
        fabricApiModules.map { project.fabricApi.module(it, fabricApiVersion) }
    }
    val modmenu = "com.terraformersmc:modmenu:$modmenuVersion"
    val yacl = "dev.isxander:yet-another-config-lib:$yaclVersion-fabric"
}

private val modMetadata = ModMetadata()
private val modLibs = ModLibraries(modMetadata)

version = "${modMetadata.modVersion}+${modMetadata.minecraftVersion}" + gitDetails.let { details ->
    "-${details.gitHash}-${details.commitDistance}".takeIf { details.commitDistance > 0 } ?: ""
}
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
}

dependencies {
    minecraft(modLibs.minecraft)
    mappings(modLibs.fabricYarn)

    modImplementation(libs.bundles.fabric)
    modLibs.fabricApi.forEach(::modImplementation)

    modImplementation(modLibs.modmenu)
    modImplementation(modLibs.yacl) {
        exclude("net.fabricmc.fabric-api", "fabric-api")
    }

    implementation(libs.bundles.kaml)
    include(libs.bundles.kaml)
}

tasks {
    processResources {
        inputs.property("version", "$version")
        inputs.property("minecraftTarget", modMetadata.minecraftTarget)
        inputs.property("javaTarget", modMetadata.javaVersion)

        filesMatching("fabric.mod.json") {
            expand(
                "version" to version,
                "minecraftTarget" to modMetadata.minecraftTarget,
                "javaTarget" to modMetadata.javaVersion,
            )
        }
    }

    jar {
        from("LICENSE")
    }
}

java {
    withSourcesJar()

    sourceCompatibility = enumValues<JavaVersion>()[modMetadata.javaVersion - 1]
    targetCompatibility = enumValues<JavaVersion>()[modMetadata.javaVersion - 1]
}

kotlin {
    jvmToolchain(modMetadata.javaVersion)
}

publishMods {
    file = tasks.remapJar.get().archiveFile
    additionalFiles.from(tasks.remapSourcesJar.get().archiveFile)
    changelog = providers.environmentVariable("CHANGELOG").orElse("No changelog provided.")
    type = when {
        "alpha" in modMetadata.modVersion -> ALPHA
        "beta" in modMetadata.modVersion -> BETA
        else -> STABLE
    }
    displayName = "[${modMetadata.minecraftVersion}] v${modMetadata.modVersion}"
    modLoaders.addAll("fabric", "quilt")

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = modMetadata.modrinthId

        minecraftVersionRange {
            start = modMetadata.minecraftTargetStart
            end = modMetadata.minecraftTargetEnd
        }

        requires("fabric-api", "fabric-language-kotlin")
        optional("modmenu", "yacl")

        tasks.getByName("publishModrinth") {
            dependsOn("optimizeOutputsOfRemapJar")
        }
    }
}

fun String.dropFirstIf(char: Char) = if (first() == char) drop(1) else this
