package dev.rvbsm.fsit.networking

import dev.rvbsm.fsit.entity.RideEntity
import dev.rvbsm.fsit.event.completeRidingRequest
import dev.rvbsm.fsit.networking.payload.ConfigUpdateC2SPayload
import dev.rvbsm.fsit.networking.payload.PoseRequestC2SPayload
import dev.rvbsm.fsit.networking.payload.RidingResponseC2SPayload
import kotlinx.coroutines.*
import net.minecraft.server.network.ServerPlayerEntity

private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

internal suspend fun ConfigUpdateC2SPayload.receive(player: ServerPlayerEntity) = coroutineScope {
    player.config = config
}

internal fun PoseRequestC2SPayload.receive(player: ServerPlayerEntity) {
    player.setPose(pose)
}

internal fun RidingResponseC2SPayload.receive(player: ServerPlayerEntity) {
    if (!response.isAccepted && player.hasPassenger { (it as? RideEntity)?.isBelongsTo(uuid) == true }) {
        player.removeAllPassengers()
    }

    completeRidingRequest(player)
}

/*? if <=1.20.4 {*/
internal fun ConfigUpdateC2SPayload.receive(
    player: ServerPlayerEntity, responseSender: net.fabricmc.fabric.api.networking.v1.PacketSender
) = scope.launch { receive(player) }

internal fun PoseRequestC2SPayload.receive(
    player: ServerPlayerEntity, responseSender: net.fabricmc.fabric.api.networking.v1.PacketSender
) = receive(player)

internal fun RidingResponseC2SPayload.receive(
    player: ServerPlayerEntity, responseSender: net.fabricmc.fabric.api.networking.v1.PacketSender
) = receive(player)
/*?} else {*/
/*internal fun ConfigUpdateC2SPayload.receive(context: ServerPlayNetworking.Context) = receive(context.player())
internal fun PoseRequestC2SPayload.receive(context: ServerPlayNetworking.Context) = receive(context.player())
internal fun RidingResponseC2SPayload.receive(context: ServerPlayNetworking.Context) = receive(context.player())
*//*?}*/
