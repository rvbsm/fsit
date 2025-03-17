import proguard.gradle.ProGuardTask

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)

    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.publish)
    alias(libs.plugins.shadow)
}

private val gitVersion: String by rootProject.ext
private val minecraftProjectVersion = stonecutter.current.project
private val minecraftTargetVersion = stonecutter.current.version
private val isMinecraftVersionRange = stonecutter.eval(minecraftTargetVersion, ">$minecraftProjectVersion")

private val javaVersion = property("java.version").toString().toInt(10)
private val modrinthId = property("mod.modrinth_id").toString()

version = "$gitVersion+mc$minecraftProjectVersion"
group = "dev.rvbsm"
base.archivesName = rootProject.name

private val devLibsPath = rootProject.layout.buildDirectory.map { it.dir("devlibs") }
private val libsPath = rootProject.layout.buildDirectory.map { it.dir("libs") }

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

repositories {
    mavenCentral()
    maven("https://maven.terraformersmc.com/releases")
    maven("https://maven.isxander.dev/releases")
    maven("https://maven.nucleoid.xyz/")
}

dependencies {
    minecraft("com.minecraft:minecraft:$minecraftTargetVersion")
    mappings("net.fabricmc:yarn:$minecraftTargetVersion+build.${property("fabric.yarn_build")}:v2")

    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.kotlin)

    setOf(
        "fabric-api-base",
        "fabric-command-api-v2",
        "fabric-key-binding-api-v1",
        "fabric-lifecycle-events-v1",
        "fabric-networking-api-v1",

        "fabric-screen-api-v1",
    ).map { fabricApi.module(it, property("fabric.api").toString()) }.forEach(::modImplementation)

    modImplementation("com.terraformersmc:modmenu:${property("api.modmenu")}")
    modImplementation("dev.isxander:yet-another-config-lib:${property("api.yacl")}-fabric") {
        exclude("net.fabricmc.fabric-api", "fabric-api")
    }

    implementation(libs.kaml)
    shadow(libs.kaml)

    testImplementation(libs.bundles.junit.jupiter)
    testImplementation(libs.fabric.loader.junit)
    testImplementation(libs.kotlin.test)
}

tasks {
    processResources {
        val properties = mapOf(
            "version" to "$version",
            "minecraftVersion" to ">=$minecraftProjectVersion-${
                " <=$minecraftTargetVersion".takeIf { isMinecraftVersionRange }.orEmpty()
            }",
            "javaVersion" to javaVersion,
        )

        inputs.properties(properties)
        filesMatching("fabric.mod.json") {
            expand(properties)
        }
    }

    jar {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${rootProject.name}" }
        }
    }

    shadowJar {
        from(jar)

        archiveClassifier = "all"
        destinationDirectory = devLibsPath

        configurations = listOf(project.configurations["shadow"])
        val relocationPath = "dev.rvbsm.fsit.lib"
        relocate("com.charleskorn.kaml", "$relocationPath.kaml")
        relocate("it.krzeminski.snakeyaml.engine.kmp", "$relocationPath.snakeyaml-kmp")
        relocate("okio", "$relocationPath.okio")
        relocate("net.thauvin.erik.urlencoder", "$relocationPath.urlencoder")

        exclude("kotlin/**")
        exclude("kotlinx/**")
        exclude("org/jetbrains/**")

        exclude("META-INF/com.android.tools/**")
        exclude("META-INF/maven/**")
        exclude("META-INF/proguard/**")
        exclude("META-INF/kotlin-*.kotlin_module")
        exclude("META-INF/kotlinx-*.kotlin_module")

        minimize()
    }

    remapJar {
        dependsOn(shadowJar)

        archiveClassifier = "dev"
        destinationDirectory = devLibsPath

        inputFile.set(shadowJar.flatMap { it.archiveFile })
    }

    remapSourcesJar {
        destinationDirectory = libsPath
    }

    build {
        finalizedBy(proguardJar)
    }

    if (stonecutter.current.isActive) register("buildActive") {
        group = "project"
        dependsOn(build)
    }

    test {
        useJUnitPlatform()
    }
}

val proguardJar by tasks.registering(ProGuardTask::class) {
    group = "project"
    dependsOn(tasks.remapJar)
    mustRunAfter(stonecutter.versions.takeWhile {
        stonecutter.eval(stonecutter.current.project, ">${it.project}")
    }.map { ":${it.project}:$name" })

    configuration("$rootDir/proguard.txt")

    injars(tasks.remapJar)
    outjars(libsPath.map { it.file("${rootProject.name}-$version.jar") })

    // blackd "~ the GOAT"
    // https://github.com/blackd/Inventory-Profiles/blob/c66b8adf57684d94eb272eb741864e74d78f522f/platforms/fabric-1.21/build.gradle.kts#L254-L257
    doFirst {
        libraryjars(
            configurations.compileClasspath.map { it.files }
                .zip(configurations.runtimeClasspath.map { it.files }, Set<File>::plus)
        )
    }
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
}

kotlin {
    jvmToolchain(javaVersion)
}

publishMods {
    file = proguardJar.map { it.outputs.files.singleFile }
    additionalFiles.from(tasks.remapSourcesJar.map { it.archiveFile })
    changelog = providers.environmentVariable("CHANGELOG")
    type = when {
        "alpha" in gitVersion -> ALPHA
        "beta" in gitVersion -> BETA
        else -> STABLE
    }
    displayName = "[$minecraftProjectVersion] v$gitVersion"
    modLoaders.addAll("fabric", "quilt")

    dryRun = !providers.environmentVariable("MODRINTH_TOKEN").isPresent

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = modrinthId
        featured = true

        minecraftVersionRange {
            start = minecraftProjectVersion
            end = minecraftTargetVersion
        }

        requires("fabric-api", "fabric-language-kotlin")
        optional("modmenu", "yacl")
    }
}
