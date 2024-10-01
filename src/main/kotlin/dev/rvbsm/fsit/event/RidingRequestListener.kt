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
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import java.util.*

private const val TIMEOUT = 5000L
private val scope = CoroutineScope(Dispatchers.IO)
private val requests = mutableMapOf<UUID, Channel<Boolean>>()

internal val StartRidingListener = PassedUseEntityCallback interact@{ player, _, entity ->
    if (!entity.isPlayer) return@interact ActionResult.PASS

    val target = entity as ServerPlayerEntity
    if (!player.canStartRiding(target)) return@interact ActionResult.PASS

    val playerConfig = player.config.onUse.takeIf { it.riding } ?: return@interact ActionResult.PASS
    val targetConfig = target.config.onUse.takeIf { it.riding } ?: return@interact ActionResult.PASS

    if (!player.isInRange(target, playerConfig.range.toDouble())) {
        return@interact ActionResult.PASS
    }

    if (player.uuid xor target.uuid !in requests) {
        sendRequests(player, target)
    }

    return@interact ActionResult.SUCCESS
}

internal val RidingServerStoppingEvent = ServerLifecycleEvents.ServerStopping {
    val cancelException = CancellationException("Server stopping")
    requests.forEach { it.value.cancel(cancelException) }
}

private fun sendRequests(player: ServerPlayerEntity, target: ServerPlayerEntity) {
    val channel = Channel<Boolean>(capacity = 2)
    requests[player.uuid xor target.uuid] = channel

    player.trySend(RidingRequestS2CPayload(target.uuid)) { channel.trySend(true) }
    target.trySend(RidingRequestS2CPayload(player.uuid)) { channel.trySend(true) }

    scope.launch {
        player.startRiding(target, channel)
    }.invokeOnCompletion { requests.remove(player.uuid xor target.uuid)?.cancel() }
}

private suspend fun ServerPlayerEntity.startRiding(target: ServerPlayerEntity, channel: ReceiveChannel<Boolean>) =
    withTimeout(TIMEOUT) {
        val playerResult = channel.receive()
        val targetResult = channel.receive()

        if (playerResult && isAlive && targetResult && target.isAlive) {
            ensureActive()

            server.execute {
                RideEntity.create(this@startRiding, target)
                target.networkHandler.sendPacket(EntityPassengersSetS2CPacket(target))
            }
        }
    }

private fun ServerPlayerEntity.shouldCancelRiding() = shouldCancelInteraction() || isSpectator || hasPassengers()

private fun ServerPlayerEntity.canStartRiding(other: ServerPlayerEntity) =
    this != other && uuid != other.uuid && !shouldCancelRiding() && !other.shouldCancelRiding()

internal fun RidingResponseC2SPayload.completeRideRequest(player: ServerPlayerEntity) {
    requests[player.uuid xor uuid]?.trySend(response.isAccepted)
}
