package dev.rvbsm.fsit.networking.payload

import dev.rvbsm.fsit.config.ModConfig
import dev.rvbsm.fsit.config.orDefault
import dev.rvbsm.fsit.jsonSerializer
import dev.rvbsm.fsit.serialization.decode
import dev.rvbsm.fsit.serialization.encode
import net.minecraft.network.NetworkSide
import net.minecraft.network.PacketByteBuf

data class ConfigUpdateC2SPayload(val serializedConfig: String) : CustomPayload<ConfigUpdateC2SPayload>(packetId) {
    override fun write(buf: PacketByteBuf) {
        buf.writeString(serializedConfig)
    }

    suspend fun decode() = jsonSerializer.decode<ModConfig>(serializedConfig).orDefault()

    companion object : Id<ConfigUpdateC2SPayload>("config_sync", NetworkSide.SERVERBOUND) {
        override fun init(buf: PacketByteBuf) = ConfigUpdateC2SPayload(buf.readString())

        suspend fun encode(config: ModConfig) = ConfigUpdateC2SPayload(jsonSerializer.encode(config).getOrThrow())
    }
}
