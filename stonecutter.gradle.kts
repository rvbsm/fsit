buildscript {
    dependencies.classpath("com.guardsquare:proguard-gradle:7.5.0")
}

plugins {
    id("dev.kikugie.stonecutter")

    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false

    alias(libs.plugins.fabric.loom) apply false
    alias(libs.plugins.publish) apply false
    alias(libs.plugins.shadow) apply false
}

stonecutter active "1.20" /* [SC] DO NOT EDIT */

tasks {
    stonecutter registerChiseled register("chiseledBuild", stonecutter.chiseled) {
        group = "project"
        ofTask("build")
    }

    stonecutter registerChiseled register("chiseledPublish", stonecutter.chiseled) {
        group = "project"
        ofTask("publishMods")
    }
}

stonecutter parameters {
    swaps["ServerPlayNetworking.PlayPayloadHandler"] = if (eval(metadata.version, "<=1.20.4")) {
        "net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPacketHandler<P>"
    } else {
        "net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPayloadHandler<P>"
    }
    swaps["ClientPlayNetworking.PlayPayloadHandler"] = if (eval(metadata.version, "<=1.20.4")) {
        "net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPacketHandler<P>"
    } else {
        "net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPayloadHandler<P>"
    }

    swaps["CustomPayload"] = if (eval(metadata.version, "<=1.20.4")) {
        "net.fabricmc.fabric.api.networking.v1.FabricPacket"
    } else {
        "net.minecraft.network.packet.CustomPayload"
    }

    swaps["CustomPayload.Id"] = if (eval(metadata.version, "<=1.20.4")) {
        "net.fabricmc.fabric.api.networking.v1.PacketType<P>"
    } else {
        "net.minecraft.network.packet.CustomPayload.Id<P>"
    }

    swaps["EntityPositionSyncS2CPacket.create"] = if (eval(metadata.version, "<=1.21.1")) {
        "net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket(this)"
    } else {
        "net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket.create(this)"
    }
}

val gitVersion: String by extra {
    providers.exec {
        executable = "git"
        args = listOf("describe", "--tags", "--dirty", "--always")
    }.standardOutput.asText.map { it.trim().drop(1) }.get()
}
