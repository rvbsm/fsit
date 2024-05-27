package dev.rvbsm.fsit.network.packet

import dev.rvbsm.fsit.FSitMod
import dev.rvbsm.fsit.compat.CustomPayload
import dev.rvbsm.fsit.entity.PlayerPose
import net.minecraft.network.PacketByteBuf

data class PoseRequestC2SPayload(val pose: PlayerPose) : CustomPayload(packetId) {
    constructor(buf: PacketByteBuf) : this(buf.readEnumConstant(PlayerPose::class.java))

    override fun write(buf: PacketByteBuf) {
        buf.writeEnumConstant(pose)
    }

    companion object {
        private val id = FSitMod.id("pose_request")

        /*? if <=1.20.4 {*//*
        val packetId = net.fabricmc.fabric.api.networking.v1.PacketType.create(id, ::PoseRequestC2SPayload)
        *//*?} else {*/
        val packetId = net.minecraft.network.packet.CustomPayload.Id<PoseRequestC2SPayload>(id)
        val packetCodec: net.minecraft.network.codec.PacketCodec<PacketByteBuf, PoseRequestC2SPayload> =
            net.minecraft.network.packet.CustomPayload.codecOf(PoseRequestC2SPayload::write, ::PoseRequestC2SPayload)

        init {
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S().register(packetId, packetCodec)
        }
        /*?} */
    }
}
