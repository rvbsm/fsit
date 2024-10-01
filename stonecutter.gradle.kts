plugins {
    id("dev.kikugie.stonecutter")
    alias(libs.plugins.fabric.loom) apply false
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
