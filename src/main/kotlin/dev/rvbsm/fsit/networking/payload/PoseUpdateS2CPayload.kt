package dev.rvbsm.fsit.networking.payload

import dev.rvbsm.fsit.entity.PlayerPose
import dev.rvbsm.fsit.networking.readEnumConstant
import net.minecraft.network.NetworkSide
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.Vec3d

data class PoseUpdateS2CPayload(val pose: PlayerPose, val pos: Vec3d) : CustomPayload<PoseUpdateS2CPayload>(packetId) {
    override fun write(buf: PacketByteBuf) {
        buf.writeEnumConstant(pose)
        buf.writeVector3f(pos.toVector3f())
    }

    companion object : Id<PoseUpdateS2CPayload>("pose_sync", NetworkSide.CLIENTBOUND) {
        override fun init(buf: PacketByteBuf) = PoseUpdateS2CPayload(buf.readEnumConstant<PlayerPose>(), Vec3d(buf.readVector3f()))
    }
}
