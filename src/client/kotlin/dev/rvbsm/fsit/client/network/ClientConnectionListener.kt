package dev.rvbsm.fsit.client.network

import dev.rvbsm.fsit.FSitMod
import dev.rvbsm.fsit.network.packet.ConfigUpdateC2SPacket
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler

object ClientConnectionListener : ClientPlayConnectionEvents.Join {
    override fun onPlayReady(handler: ClientPlayNetworkHandler, sender: PacketSender, client: MinecraftClient) {
        sender.sendPacket(ConfigUpdateC2SPacket(FSitMod.config))
    }
}