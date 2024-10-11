package dev.rvbsm.fsit.networking.payload

import dev.rvbsm.fsit.config.ModConfig
import dev.rvbsm.fsit.config.serialization.ConfigSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import net.minecraft.network.NetworkSide
import net.minecraft.network.PacketByteBuf

@OptIn(ExperimentalSerializationApi::class)
private val jsonConfigSerializer =
    ConfigSerializer(format = Json { ignoreUnknownKeys = true; namingStrategy = JsonNamingStrategy.SnakeCase })

data class ConfigUpdateC2SPayload(val config: ModConfig) : CustomPayload<ConfigUpdateC2SPayload>(packetId) {
    override fun write(buf: PacketByteBuf) {
        buf.writeString(jsonConfigSerializer.encode(config))
    }

    companion object : Id<ConfigUpdateC2SPayload>("config_sync", NetworkSide.SERVERBOUND) {
        override fun init(buf: PacketByteBuf) = ConfigUpdateC2SPayload(jsonConfigSerializer.decode(buf.readString()))
    }
}
