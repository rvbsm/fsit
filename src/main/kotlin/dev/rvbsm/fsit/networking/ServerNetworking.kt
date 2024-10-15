package dev.rvbsm.fsit.networking

import dev.rvbsm.fsit.entity.RideEntity
import dev.rvbsm.fsit.event.completeRidingRequest
import dev.rvbsm.fsit.networking.payload.ConfigUpdateC2SPayload
import dev.rvbsm.fsit.networking.payload.PoseRequestC2SPayload
import dev.rvbsm.fsit.networking.payload.RidingResponseC2SPayload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.minecraft.server.network.ServerPlayerEntity

private val payloadScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

internal fun ConfigUpdateC2SPayload.receive(
    //? if <=1.20.4
    player: ServerPlayerEntity, responseSender: net.fabricmc.fabric.api.networking.v1.PacketSender
    //? if >=1.20.5
    /*context: net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context*/
) {
    //? if >=1.20.5
    /*val player = context.player() as ServerPlayerEntity*/

    payloadScope.launch { player.config = config }
}

internal fun PoseRequestC2SPayload.receive(
    //? if <=1.20.4
    player: ServerPlayerEntity, responseSender: net.fabricmc.fabric.api.networking.v1.PacketSender
    //? if >=1.20.5
    /*context: net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context*/
) {
    //? if >=1.20.5
    /*val player = context.player() as ServerPlayerEntity*/

    player.setPose(pose)
}

internal fun RidingResponseC2SPayload.receive(
    //? if <=1.20.4
    player: ServerPlayerEntity, responseSender: net.fabricmc.fabric.api.networking.v1.PacketSender
    //? if >=1.20.5
    /*context: net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context*/
) {
    //? if >=1.20.5
    /*val player = context.player() as ServerPlayerEntity*/

    if (!response.isAccepted && player.hasPassenger { (it as? RideEntity)?.isBelongsTo(uuid) == true }) {
        player.removeAllPassengers()
    }

    completeRidingRequest(player)
}
