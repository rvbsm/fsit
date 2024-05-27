package dev.rvbsm.fsit.network.packet

import dev.rvbsm.fsit.FSitMod
import dev.rvbsm.fsit.compat.CustomPayload
import dev.rvbsm.fsit.config.ModConfig
import net.minecraft.network.PacketByteBuf

data class ConfigUpdateC2SPayload(val config: ModConfig) : CustomPayload(packetId) {
    constructor(buf: PacketByteBuf) : this(ModConfig.decodeJson(buf.readString()))

    override fun write(buf: PacketByteBuf) {
        buf.writeString(config.encodeJson())
    }

    companion object {
        private val id = FSitMod.id("config_sync")

        /*? if <=1.20.4 {*//*
        val packetId = net.fabricmc.fabric.api.networking.v1.PacketType.create(id, ::ConfigUpdateC2SPayload)
        *//*?} else {*/
        val packetId = net.minecraft.network.packet.CustomPayload.Id<ConfigUpdateC2SPayload>(id)
        val packetCodec =
            net.minecraft.network.packet.CustomPayload.codecOf(ConfigUpdateC2SPayload::write, ::ConfigUpdateC2SPayload)

        init {
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(packetId, packetCodec)
        }
        /*?} */
    }
}
