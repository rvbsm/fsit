package dev.rvbsm.fsit.event

import dev.rvbsm.fsit.entity.RideEntity
import dev.rvbsm.fsit.networking.config
import dev.rvbsm.fsit.networking.payload.RidingRequestS2CPayload
import dev.rvbsm.fsit.networking.payload.RidingResponseC2SPayload
import dev.rvbsm.fsit.networking.trySend
import dev.rvbsm.fsit.util.xor
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import java.util.*

private const val TIMEOUT = 5000L
private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
private val requests = mutableMapOf<UUID, Channel<Boolean>>()

internal val StartRidingListener = PassedUseEntityCallback interact@{ player, _, entity ->
    if (entity !is ServerPlayerEntity) return@interact ActionResult.PASS

    if (!player.canStartRiding(entity)) return@interact ActionResult.PASS

    if (!player.config.onUse.riding || !entity.config.onUse.riding) return@interact ActionResult.PASS
    else if (!player.isInRange(entity, player.config.onUse.range.toDouble())) return@interact ActionResult.PASS

    if (player.uuid xor entity.uuid !in requests) {
        sendRidingRequests(player, entity)
    }

    return@interact ActionResult.SUCCESS
}

internal val RidingServerStoppingEvent = ServerLifecycleEvents.ServerStopping {
    val cancelException = CancellationException("Server stopping")
    requests.forEach { it.value.cancel(cancelException) }
}

private fun sendRidingRequests(player: ServerPlayerEntity, target: ServerPlayerEntity) = scope.run {
    val channel = Channel<Boolean>(capacity = 2)
    requests[player.uuid xor target.uuid] = channel

    player.trySend(RidingRequestS2CPayload(target.uuid)) { channel.trySend(true) }
    target.trySend(RidingRequestS2CPayload(player.uuid)) { channel.trySend(true) }

    launch {
        player.startRiding(target, channel)
    }.invokeOnCompletion { requests.remove(player.uuid xor target.uuid)?.close() }
}

private suspend fun ServerPlayerEntity.startRiding(target: ServerPlayerEntity, channel: ReceiveChannel<Boolean>) =
    withTimeout(TIMEOUT) {
        val result = channel.receive() to channel.receive()

        if (result.first && isAlive && result.second && target.isAlive) {
            ensureActive()

            server.execute {
                RideEntity.create(this@startRiding, target)
            }
        }
    }

private fun ServerPlayerEntity.shouldCancelRiding() = shouldCancelInteraction() || isSpectator || hasPassengers()

private fun ServerPlayerEntity.canStartRiding(other: ServerPlayerEntity) =
    this != other && uuid != other.uuid && !shouldCancelRiding() && !other.shouldCancelRiding()

internal fun RidingResponseC2SPayload.completeRidingRequest(player: ServerPlayerEntity) {
    requests[player.uuid xor uuid]?.trySend(response.isAccepted)
}
