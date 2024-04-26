package dev.rvbsm.fsit.network.packet

import dev.rvbsm.fsit.FSitMod
import dev.rvbsm.fsit.compat.CustomPayload
import net.minecraft.network.PacketByteBuf
import java.util.*

data class RidingResponseC2SPayload(val uuid: UUID, val response: ResponseType) : CustomPayload(packetId) {
    constructor(uuid: UUID, isAccepted: Boolean) : this(uuid, ResponseType.valueOf(isAccepted))
    constructor(buf: PacketByteBuf) : this(buf.readUuid(), buf.readEnumConstant(ResponseType::class.java))

    override fun write(buf: PacketByteBuf) {
        buf.writeUuid(uuid)
        buf.writeEnumConstant(response)
    }

    companion object {
        private val id = FSitMod.id("riding_response")

        /*? if <=1.20.4 {*/
        val packetId = net.fabricmc.fabric.api.networking.v1.PacketType.create(id, ::RidingResponseC2SPayload)
        /*?} else {*//*
        val packetId = net.minecraft.network.packet.CustomPayload.Id<RidingResponseC2SPayload>(id)
        val packetCodec =
            net.minecraft.network.packet.CustomPayload.codecOf(RidingResponseC2SPayload::write, ::RidingResponseC2SPayload)

        init {
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(packetId, packetCodec)
        }
        *//*?} */
    }

    enum class ResponseType(val isAccepted: Boolean) {
        Accept(true), Refuse(false);

        companion object {
            fun valueOf(isAccepted: Boolean): ResponseType {
                return if (isAccepted) Accept else Refuse
            }
        }
    }
}
