package dev.rvbsm.fsit.event

import dev.rvbsm.fsit.api.event.PassedUseEntityCallback
import dev.rvbsm.fsit.entity.RideEntity
import dev.rvbsm.fsit.modScope
import dev.rvbsm.fsit.networking.config
import dev.rvbsm.fsit.networking.newRidingRequest
import dev.rvbsm.fsit.networking.payload.RidingRequestS2CPayload
import dev.rvbsm.fsit.networking.trySend
import kotlinx.coroutines.future.asDeferred
import kotlinx.coroutines.launch
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import kotlin.time.Duration.Companion.milliseconds

private val requestTimeout = 5000.milliseconds

internal val RidingRequestListener = PassedUseEntityCallback interact@{ player, _, entity ->
    if (entity !is ServerPlayerEntity || !player.canStartRiding(entity)) return@interact ActionResult.PASS

    if (!player.config.onUse.riding || !entity.config.onUse.riding) return@interact ActionResult.PASS
    else if (!player.isInRange(entity, player.config.onUse.range.toDouble())) return@interact ActionResult.PASS

    val playerResponse = player.newRidingRequest(entity.uuid, requestTimeout)
    player.trySend(RidingRequestS2CPayload(entity.uuid)) { playerResponse.complete(true) }
    val entityResponse = entity.newRidingRequest(player.uuid, requestTimeout)
    entity.trySend(RidingRequestS2CPayload(player.uuid)) { entityResponse.complete(true) }

    modScope.launch {
        val response = playerResponse.thenCombine(entityResponse, Boolean::and).asDeferred()

        if (response.await() && player.canStartRiding(entity)) {
            RideEntity.create(player, entity)
        }
    }

    //? if <=1.21.1
    return@interact ActionResult.SUCCESS
    //? if >=1.21.2
    /*return@interact ActionResult.SUCCESS_SERVER*/
}

private fun ServerPlayerEntity.shouldCancelRiding() = shouldCancelInteraction() || isSpectator || hasPassengers()

private fun ServerPlayerEntity.canStartRiding(other: ServerPlayerEntity) =
    this != other && uuid != other.uuid &&
        !shouldCancelRiding() && !other.shouldCancelRiding() &&
        config.onUse.riding && other.config.onUse.riding &&
        isInRange(other, config.onUse.range.toDouble())
