package dev.rvbsm.fsit.network.packet

import dev.rvbsm.fsit.FSitMod
import dev.rvbsm.fsit.entity.Pose
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf

data class PoseRequestC2SPacket(val pose: Pose) : FabricPacket {
    override fun write(buf: PacketByteBuf) {
        buf.writeEnumConstant(pose)
    }

    override fun getType(): PacketType<*> = pType

    companion object {
        private val id = FSitMod.id("pose_request")
        val pType: PacketType<PoseRequestC2SPacket> = PacketType.create(id) {
            PoseRequestC2SPacket(it.readEnumConstant(Pose::class.java))
        }
    }
}
