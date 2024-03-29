package dev.rvbsm.fsit.network.packet

import dev.rvbsm.fsit.FSitMod
import dev.rvbsm.fsit.entity.Pose
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.Vec3d

data class PoseUpdateS2CPacket(val pose: Pose, val pos: Vec3d) : FabricPacket {
    override fun write(buf: PacketByteBuf) {
        buf.writeEnumConstant(pose)
        buf.writeVector3f(pos.toVector3f())
    }

    override fun getType(): PacketType<*> = pType

    companion object {
        private val id = FSitMod.id("pose_sync")
        val pType: PacketType<PoseUpdateS2CPacket> = PacketType.create(id) {
            PoseUpdateS2CPacket(it.readEnumConstant(Pose::class.java), Vec3d(it.readVector3f()))
        }
    }
}
