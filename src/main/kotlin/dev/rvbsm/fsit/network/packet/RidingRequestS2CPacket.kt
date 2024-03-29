package dev.rvbsm.fsit.network.packet

import dev.rvbsm.fsit.FSitMod
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf
import java.util.*

data class RidingRequestS2CPacket(val uuid: UUID) : FabricPacket {
    override fun write(buf: PacketByteBuf) {
        buf.writeUuid(uuid)
    }

    override fun getType(): PacketType<*> = pType

    companion object {
        private val id = FSitMod.id("riding_request")
        val pType: PacketType<RidingRequestS2CPacket> = PacketType.create(id) {
            RidingRequestS2CPacket(it.readUuid())
        }
    }
}
