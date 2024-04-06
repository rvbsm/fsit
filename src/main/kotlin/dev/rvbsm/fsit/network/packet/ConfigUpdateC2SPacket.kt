package dev.rvbsm.fsit.network.packet

import dev.rvbsm.fsit.FSitMod
import dev.rvbsm.fsit.config.ModConfig
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf

data class ConfigUpdateC2SPacket(val config: ModConfig) : FabricPacket {
    override fun write(buf: PacketByteBuf) {
        buf.writeString(config.toJson())
    }

    override fun getType(): PacketType<*> = pType

    companion object {
        private val id = FSitMod.id("config_sync")
        val pType: PacketType<ConfigUpdateC2SPacket> = PacketType.create(id) {
            ConfigUpdateC2SPacket(ModConfig.fromJson(it.readString()))
        }
    }
}
