package dev.rvbsm.fsit.network.packet

import dev.rvbsm.fsit.FSitMod
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf
import java.util.*

data class RidingResponseC2SPacket(val uuid: UUID, val response: ResponseType) : FabricPacket {
    constructor(uuid: UUID, isAccepted: Boolean) : this(uuid, ResponseType.valueOf(isAccepted))

    override fun write(buf: PacketByteBuf) {
        buf.writeUuid(uuid)
        buf.writeEnumConstant(response)
    }

    override fun getType(): PacketType<*> = pType

    companion object {
        private val id = FSitMod.id("riding_response")
        val pType: PacketType<RidingResponseC2SPacket> = PacketType.create(id) {
            RidingResponseC2SPacket(it.readUuid(), it.readEnumConstant(ResponseType::class.java))
        }
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
