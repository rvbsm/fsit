buildscript {
    dependencies.classpath("com.guardsquare:proguard-gradle:7.5.0")
}

plugins {
    id("dev.kikugie.stonecutter")

    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.plugin.serialization) apply false

    alias(libs.plugins.fabric.loom) apply false
    alias(libs.plugins.publish) apply false
    alias(libs.plugins.shadow) apply false
}
stonecutter active "1.20" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    group = "project"
    ofTask("build")
}

stonecutter registerChiseled tasks.register("chiseledPublish", stonecutter.chiseled) {
    group = "project"
    ofTask("publishMods")
}
