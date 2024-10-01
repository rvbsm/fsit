import proguard.gradle.ProGuardTask

buildscript {
    dependencies.classpath("com.guardsquare:proguard-gradle:7.5.0")
}

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)

    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.publish)
    alias(libs.plugins.shadow)
}

private val modVersion = "git describe --tags".runCommand()?.dropFirstIf('v')!!
private val minecraftLeastCompatibleVersion = stonecutter.current.project
private val minecraftVersion = stonecutter.current.version
private val isLeastEqualsCurrent = minecraftLeastCompatibleVersion == minecraftVersion

private val javaVersion = property("java.version").toString().toInt(10)
private val modrinthId = property("mod.modrinth_id").toString()

private class ModLibraries {
    private val fabricYarnBuild = "${property("fabric.yarn_build")}"
    private val fabricApiVersion = "${property("fabric.api")}+$minecraftVersion"
    private val fabricApiModules = setOf(
        "fabric-api-base",
        "fabric-command-api-v2",
        "fabric-key-binding-api-v1",
        "fabric-lifecycle-events-v1",
        "fabric-networking-api-v1",

        "fabric-screen-api-v1", // bruh
    )
    private val modmenuVersion = "${property("api.modmenu")}"
    private val yaclVersion = "${property("api.yacl")}"

    val minecraft = "com.mojang:minecraft:$minecraftVersion"
    val fabricYarn = "net.fabricmc:yarn:$minecraftVersion+build.$fabricYarnBuild:v2"
    val fabricApi by lazy { fabricApiModules.map { project.fabricApi.module(it, fabricApiVersion) } }
    val modmenu = "com.terraformersmc:modmenu:$modmenuVersion"
    val yacl = "dev.isxander:yet-another-config-lib:$yaclVersion-fabric"
}

private val modLibs = ModLibraries()

version = "$modVersion+mc$minecraftLeastCompatibleVersion"
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
}

sourceSets.test {
    compileClasspath += sourceSets["client"].compileClasspath
    runtimeClasspath += sourceSets["client"].runtimeClasspath
}

val shadowInclude: Configuration by configurations.creating

repositories {
    mavenCentral()
    maven("https://maven.terraformersmc.com/releases")
    maven("https://maven.isxander.dev/releases")
}

dependencies {
    minecraft(modLibs.minecraft)
    mappings(modLibs.fabricYarn)

    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.kotlin)

    modLibs.fabricApi.forEach(::modImplementation)

    modImplementation(modLibs.modmenu)
    modImplementation(modLibs.yacl) {
        exclude("net.fabricmc.fabric-api", "fabric-api")
    }

    implementation(libs.kaml)
    shadowInclude(libs.kaml)

    testImplementation(libs.fabric.loader.junit)
    testImplementation(libs.kotlin.test)
}

tasks {
    processResources {
        val properties = mapOf(
            "version" to "$version",
            "minecraftVersion" to ">=$minecraftLeastCompatibleVersion${
                " <=$minecraftVersion".takeUnless { isLeastEqualsCurrent }.orEmpty()
            }",
            "javaVersion" to javaVersion,
        )

        inputs.properties(properties)
        filesMatching("fabric.mod.json") {
            expand(properties)
        }
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${rootProject.name}" }
        }
    }

    shadowJar {
        from(jar)

        archiveClassifier = "all"
        configurations = listOf(shadowInclude)

        relocate("it.krzeminski.snakeyaml.engine.kmp", "dev.rvbsm.fsit.lib.snakeyaml-kmp")
        relocate("com.charleskorn.kaml", "dev.rvbsm.fsit.lib.kaml")
        relocate("okio", "dev.rvbsm.fsit.lib.okio")
        relocate("net.thauvin.erik.urlencoder", "dev.rvbsm.fsit.lib.urlencoder")

        exclude("kotlin/**")
        exclude("kotlinx/**")
        exclude("org/jetbrains/**")

        exclude("META-INF/com.android.tools/**")
        exclude("META-INF/maven/**")
        exclude("META-INF/proguard/**")
        exclude("META-INF/versions/**")
        exclude("META-INF/kotlin-*.kotlin_module")
        exclude("META-INF/kotlinx-*.kotlin_module")

        minimize()
    }

    remapJar {
        dependsOn(shadowJar)
        archiveClassifier = "dev"

        inputFile.set(shadowJar.get().archiveFile)
    }

    val proguardJar by registering(ProGuardTask::class) {
        dependsOn(remapJar)

        verbose()
        dontwarn()

        injars(remapJar)
        outjars(layout.buildDirectory.file("libs/${rootProject.name}-$version.jar"))

        libraryjars("${System.getProperty("java.home")}/jmods")

        dontobfuscate()

        keepattributes()
        keepparameternames()
        keepkotlinmetadata()

        keep("class !dev.rvbsm.fsit.lib.** { *; }")

        // blackd "~ the GOAT"
        // https://github.com/blackd/Inventory-Profiles/blob/c66b8adf57684d94eb272eb741864e74d78f522f/platforms/fabric-1.21/build.gradle.kts#L254-L257
        doFirst {
            libraryjars(configurations.compileClasspath.get().files + configurations.runtimeClasspath.get().files)
        }
    }

    build {
        finalizedBy(proguardJar)
    }

    test {
        useJUnitPlatform()
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

publishMods {
    file = tasks.remapJar.get().archiveFile
    additionalFiles.from(tasks.remapSourcesJar.get().archiveFile)
    changelog = providers.environmentVariable("CHANGELOG").orElse("No changelog provided.")
    type = when {
        "alpha" in modVersion -> ALPHA
        "beta" in modVersion -> BETA
        else -> STABLE
    }
    displayName = "[$minecraftVersion] v$modVersion}"
    modLoaders.addAll("fabric", "quilt")

    dryRun = !providers.environmentVariable("MODRINTH_TOKEN").isPresent

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = modrinthId
        featured = true

        if (isLeastEqualsCurrent) {
            minecraftVersions.add(minecraftVersion)
        } else minecraftVersionRange {
            start = minecraftLeastCompatibleVersion
            end = minecraftVersion
        }

        requires("fabric-api", "fabric-language-kotlin")
        optional("modmenu", "yacl")
    }
}

private fun String.dropFirstIf(char: Char) = if (first() == char) drop(1) else this

private fun String.runCommand() = runCatching {
    ProcessBuilder(split(" "))
        .start().apply { waitFor(10, TimeUnit.SECONDS) }
        .inputStream.bufferedReader().readText().trim()
}.onFailure { it.printStackTrace() }.getOrNull()
