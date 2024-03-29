package dev.rvbsm.fsit.network.packet

import dev.rvbsm.fsit.FSitMod
import dev.rvbsm.fsit.config.ModConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf

private val json = Json { ignoreUnknownKeys = true }

data class ConfigUpdateC2SPacket(val config: ModConfig) : FabricPacket {
    override fun write(buf: PacketByteBuf) {
        buf.writeString(Json.encodeToString(config))
    }

    override fun getType(): PacketType<*> = pType

    companion object {
        private val id = FSitMod.id("config_sync")
        val pType: PacketType<ConfigUpdateC2SPacket> = PacketType.create(id) {
            ConfigUpdateC2SPacket(json.decodeFromString(it.readString()))
        }
    }
}
