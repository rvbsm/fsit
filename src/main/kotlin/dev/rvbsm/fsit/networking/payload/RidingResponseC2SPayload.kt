package dev.rvbsm.fsit.networking.payload

import dev.rvbsm.fsit.networking.readEnumConstant
import net.minecraft.network.NetworkSide
import net.minecraft.network.PacketByteBuf
import java.util.UUID

data class RidingResponseC2SPayload(val uuid: UUID, val response: ResponseType) :
    CustomPayload<RidingResponseC2SPayload>(packetId) {
    constructor(uuid: UUID, isAccepted: Boolean) : this(uuid, ResponseType.valueOf(isAccepted))

    override fun write(buf: PacketByteBuf) {
        buf.writeUuid(uuid)
        buf.writeEnumConstant(response)
    }

    companion object : Id<RidingResponseC2SPayload>("riding_response", NetworkSide.SERVERBOUND) {
        override fun init(buf: PacketByteBuf) =
            RidingResponseC2SPayload(buf.readUuid(), buf.readEnumConstant<ResponseType>())
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
