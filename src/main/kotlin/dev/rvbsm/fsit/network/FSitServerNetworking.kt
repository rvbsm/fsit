package dev.rvbsm.fsit.network

import dev.rvbsm.fsit.event.UseEntityListener
import dev.rvbsm.fsit.network.packet.ConfigUpdateC2SPacket
import dev.rvbsm.fsit.network.packet.PoseRequestC2SPacket
import dev.rvbsm.fsit.network.packet.RidingResponseC2SPacket
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import org.slf4j.LoggerFactory

object FSitServerNetworking {
    private val logger = LoggerFactory.getLogger(FSitServerNetworking::class.java)

    fun register() {
        ServerPlayNetworking.registerGlobalReceiver(ConfigUpdateC2SPacket.pType, ::onReceiveConfig)
        ServerPlayNetworking.registerGlobalReceiver(PoseRequestC2SPacket.pType, ::onReceivePoseRequest)
        ServerPlayNetworking.registerGlobalReceiver(RidingResponseC2SPacket.pType, ::onReceiveRidingResponse)
    }

    private fun onReceiveConfig(packet: ConfigUpdateC2SPacket, player: PlayerEntity, sender: PacketSender) {
        (player as ServerPlayerEntity).setConfig(packet.config)
    }

    private fun onReceiveRidingResponse(packet: RidingResponseC2SPacket, player: PlayerEntity, sender: PacketSender) {
        // todo: should it has an independent packet?
        if (!packet.response.isAccepted && player.hasPassenger { it.uuid == packet.uuid }) {
            player.removeAllPassengers()
        }

        UseEntityListener.receiveResponse(packet, player)
    }

    private fun onReceivePoseRequest(packet: PoseRequestC2SPacket, player: PlayerEntity, sender: PacketSender) {
        (player as ServerPlayerEntity).setPose(packet.pose)
    }
}
