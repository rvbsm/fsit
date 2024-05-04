package dev.rvbsm.fsit.network.packet

import dev.rvbsm.fsit.FSitMod
import dev.rvbsm.fsit.compat.CustomPayload
import dev.rvbsm.fsit.entity.PlayerPose
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.Vec3d

data class PoseUpdateS2CPayload(val pose: PlayerPose, val pos: Vec3d) : CustomPayload(packetId) {
    constructor(buf: PacketByteBuf) : this(buf.readEnumConstant(PlayerPose::class.java), Vec3d(buf.readVector3f()))

    override fun write(buf: PacketByteBuf) {
        buf.writeEnumConstant(pose)
        buf.writeVector3f(pos.toVector3f())
    }

    companion object {
        private val id = FSitMod.id("pose_sync")

        /*? if <=1.20.4 {*/
        val packetId = net.fabricmc.fabric.api.networking.v1.PacketType.create(id, ::PoseUpdateS2CPayload)
        /*?} else {*//*
        val packetId = net.minecraft.network.packet.CustomPayload.Id<PoseUpdateS2CPayload>(id)
        val packetCodec =
            net.minecraft.network.packet.CustomPayload.codecOf(PoseUpdateS2CPayload::write, ::PoseUpdateS2CPayload)

        init {
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C().register(packetId, packetCodec)
        }
        *//*?} */
    }
}
