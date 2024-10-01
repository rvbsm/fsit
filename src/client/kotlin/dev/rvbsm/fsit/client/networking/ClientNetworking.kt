package dev.rvbsm.fsit.client.networking

import dev.rvbsm.fsit.client.FSitModClient
import dev.rvbsm.fsit.client.config.RestrictionList
import dev.rvbsm.fsit.client.option.FSitKeyBindings
import dev.rvbsm.fsit.networking.payload.PoseUpdateS2CPayload
import dev.rvbsm.fsit.networking.payload.RidingRequestS2CPayload
import dev.rvbsm.fsit.networking.payload.RidingResponseC2SPayload
import net.minecraft.client.network.ClientPlayerEntity

internal fun PoseUpdateS2CPayload.receive(player: ClientPlayerEntity) {
    if (player.pose() != pose) {
        player.setPose(pose)
        FSitKeyBindings.reset()
    }
}

internal fun RidingRequestS2CPayload.receive() {
    FSitModClient.trySend(RidingResponseC2SPayload(uuid, !RestrictionList.isRestricted(uuid)))
}

/*? if <=1.20.4 {*/
internal fun PoseUpdateS2CPayload.receive(
    player: ClientPlayerEntity, responseSender: net.fabricmc.fabric.api.networking.v1.PacketSender
) = receive(player)

internal fun RidingRequestS2CPayload.receive(
    player: ClientPlayerEntity, responseSender: net.fabricmc.fabric.api.networking.v1.PacketSender
) = receive()
/*?} else {*/
/*internal fun PoseUpdateS2CPayload.receive(context: ClientPlayNetworking.Context) = receive(context.player())
internal fun RidingRequestS2CPayload.receive(context: ClientPlayNetworking.Context) = receive()
*//*?}*/
