package dev.rvbsm.fsit.network.packet

import dev.rvbsm.fsit.FSitMod
import dev.rvbsm.fsit.compat.CustomPayload
import net.minecraft.network.PacketByteBuf
import java.util.*

data class RidingRequestS2CPayload(val uuid: UUID) : CustomPayload(packetId) {
    constructor(buf: PacketByteBuf) : this(buf.readUuid())

    override fun write(buf: PacketByteBuf) {
        buf.writeUuid(uuid)
    }

    companion object {
        private val id = FSitMod.id("riding_request")

        /*? if <=1.20.4 {*//*
        val packetId = net.fabricmc.fabric.api.networking.v1.PacketType.create(id, ::RidingRequestS2CPayload)
        *//*?} else {*/
        val packetId = net.minecraft.network.packet.CustomPayload.Id<RidingRequestS2CPayload>(id)
        val packetCodec: net.minecraft.network.codec.PacketCodec<PacketByteBuf, RidingRequestS2CPayload> =
            net.minecraft.network.packet.CustomPayload.codecOf(RidingRequestS2CPayload::write, ::RidingRequestS2CPayload)

        init {
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(packetId, packetCodec)
        }
        /*?} */
    }
}
