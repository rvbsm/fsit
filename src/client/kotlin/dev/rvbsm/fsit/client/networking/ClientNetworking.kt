package dev.rvbsm.fsit.client.networking

import dev.rvbsm.fsit.client.FSitModClient
import dev.rvbsm.fsit.client.config.RestrictionList
import dev.rvbsm.fsit.client.option.FSitKeyBindings
import dev.rvbsm.fsit.networking.payload.PoseUpdateS2CPayload
import dev.rvbsm.fsit.networking.payload.RidingRequestS2CPayload
import dev.rvbsm.fsit.networking.payload.RidingResponseC2SPayload
import net.minecraft.client.network.ClientPlayerEntity

internal fun PoseUpdateS2CPayload.receive(
    //? if <=1.20.4
    player: ClientPlayerEntity, responseSender: net.fabricmc.fabric.api.networking.v1.PacketSender
    //? if >=1.20.5
    /*context: net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context*/
) {
    //? if >=1.20.5
    /*val player = context.player()*/

    if (player.pose() != pose) {
        player.setPose(pose)
        FSitKeyBindings.reset()
    }
}

internal fun RidingRequestS2CPayload.receive(
    //? if <=1.20.4
    player: ClientPlayerEntity, responseSender: net.fabricmc.fabric.api.networking.v1.PacketSender
    //? if >=1.20.5
    /*context: net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context*/
) {
    //? if >=1.20.5
    /*val player = context.player()*/

    FSitModClient.trySend(RidingResponseC2SPayload(uuid, !RestrictionList.isRestricted(uuid)))
}
