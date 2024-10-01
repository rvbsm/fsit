package dev.rvbsm.fsit.networking.payload

import dev.rvbsm.fsit.entity.PlayerPose
import dev.rvbsm.fsit.networking.readEnumConstant
import net.minecraft.network.NetworkSide
import net.minecraft.network.PacketByteBuf

data class PoseRequestC2SPayload(val pose: PlayerPose) : CustomPayload<PoseRequestC2SPayload>(packetId) {
    override fun write(buf: PacketByteBuf) {
        buf.writeEnumConstant(pose)
    }

    internal companion object : Id<PoseRequestC2SPayload>("pose_request", NetworkSide.SERVERBOUND) {
        override fun init(buf: PacketByteBuf) = PoseRequestC2SPayload(buf.readEnumConstant<PlayerPose>())
    }
}
