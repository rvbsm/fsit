package dev.rvbsm.fsit.client.network

import dev.rvbsm.fsit.client.FSitModClient
import dev.rvbsm.fsit.client.config.RestrictionList
import dev.rvbsm.fsit.client.network.FSitClientNetworking.receive
import dev.rvbsm.fsit.client.option.FSitKeyBindings
import dev.rvbsm.fsit.network.packet.PoseUpdateS2CPayload
import dev.rvbsm.fsit.network.packet.RidingRequestS2CPayload
import dev.rvbsm.fsit.network.packet.RidingResponseC2SPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.network.ClientPlayerEntity
import org.slf4j.LoggerFactory

object FSitClientNetworking {
    private val logger = LoggerFactory.getLogger(FSitClientNetworking::class.java)

    fun register() {
        ClientPlayNetworking.registerGlobalReceiver(PoseUpdateS2CPayload.packetId, PoseUpdateS2CPayload::receive)
        ClientPlayNetworking.registerGlobalReceiver(RidingRequestS2CPayload.packetId, RidingRequestS2CPayload::receive)
    }

    internal fun PoseUpdateS2CPayload.receive(player: ClientPlayerEntity) {
        if (player.pose() != pose) {
            player.setPose(pose)
            FSitKeyBindings.reset()
        }
    }

    internal fun RidingRequestS2CPayload.receive() {
        FSitModClient.trySend(RidingResponseC2SPayload(uuid, !RestrictionList.isRestricted(uuid)))
    }
}

/*? if <=1.20.4 {*/
private fun PoseUpdateS2CPayload.receive(
    player: ClientPlayerEntity, responseSender: net.fabricmc.fabric.api.networking.v1.PacketSender
) = receive(player)

private fun RidingRequestS2CPayload.receive(
    player: ClientPlayerEntity, responseSender: net.fabricmc.fabric.api.networking.v1.PacketSender
) = receive()
/*?} else {*/
/*private fun PoseUpdateS2CPayload.receive(context: ClientPlayNetworking.Context) = receive(context.player())
private fun RidingRequestS2CPayload.receive(context: ClientPlayNetworking.Context) = receive()
*//*?}*/
