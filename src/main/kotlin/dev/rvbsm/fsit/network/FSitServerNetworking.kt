package dev.rvbsm.fsit.network

import dev.rvbsm.fsit.event.PassedUseEntityCallback.Companion.completeRideRequest
import dev.rvbsm.fsit.network.FSitServerNetworking.receive
import dev.rvbsm.fsit.network.packet.ConfigUpdateC2SPayload
import dev.rvbsm.fsit.network.packet.PoseRequestC2SPayload
import dev.rvbsm.fsit.network.packet.RidingResponseC2SPayload
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.network.ServerPlayerEntity
import org.slf4j.LoggerFactory

object FSitServerNetworking {
    private val logger = LoggerFactory.getLogger(FSitServerNetworking::class.java)

    fun register() {
        ServerPlayNetworking.registerGlobalReceiver(ConfigUpdateC2SPayload.packetId, ConfigUpdateC2SPayload::receive)
        ServerPlayNetworking.registerGlobalReceiver(PoseRequestC2SPayload.packetId, PoseRequestC2SPayload::receive)
        ServerPlayNetworking.registerGlobalReceiver(
            RidingResponseC2SPayload.packetId, RidingResponseC2SPayload::receive
        )
    }

    internal fun ConfigUpdateC2SPayload.receive(player: ServerPlayerEntity) {
        player.setConfig(config)
    }

    internal fun PoseRequestC2SPayload.receive(player: ServerPlayerEntity) {
        player.setPose(pose)
    }

    internal fun RidingResponseC2SPayload.receive(player: ServerPlayerEntity) {
        if (!response.isAccepted && player.hasPassenger { it.uuid == uuid }) {
            player.removeAllPassengers()
        }

        completeRideRequest(player)
    }
}

/*? if <=1.20.4 {*/
private fun ConfigUpdateC2SPayload.receive(
    player: ServerPlayerEntity, responseSender: net.fabricmc.fabric.api.networking.v1.PacketSender
) = receive(player)

private fun PoseRequestC2SPayload.receive(
    player: ServerPlayerEntity, responseSender: net.fabricmc.fabric.api.networking.v1.PacketSender
) = receive(player)

private fun RidingResponseC2SPayload.receive(
    player: ServerPlayerEntity, responseSender: net.fabricmc.fabric.api.networking.v1.PacketSender
) = receive(player)
/*?} else {*//*
private fun ConfigUpdateC2SPayload.receive(context: ServerPlayNetworking.Context) = receive(context.player())
private fun PoseRequestC2SPayload.receive(context: ServerPlayNetworking.Context) = receive(context.player())
private fun RidingResponseC2SPayload.receive(context: ServerPlayNetworking.Context) = receive(context.player())
*//*?} */
