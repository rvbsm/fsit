package dev.rvbsm.fsit.event

import dev.rvbsm.fsit.entity.PlayerPose
import dev.rvbsm.fsit.entity.RideEntity
import dev.rvbsm.fsit.networking.config
import dev.rvbsm.fsit.networking.setPose
import kotlinx.coroutines.*
import net.minecraft.entity.EntityPose
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Direction
import java.util.*

private val scope = CoroutineScope(Dispatchers.IO)
private val sneaks = mutableMapOf<UUID, Job>()

val ClientCommandSneakListener = ClientCommandCallback onClientCommand@{ player, mode ->
    if (mode == ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY && player.firstPassenger is RideEntity) {
        return@onClientCommand player.removeAllPassengers()
    }

    if (mode != ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY || !player.isOnGround) return@onClientCommand

    val config = player.config.onDoubleSneak.takeUnless { !it.sitting && !it.crawling } ?: return@onClientCommand
    if (player.pitch < config.minPitch) return@onClientCommand

    if (player.uuid !in sneaks) {
        sneaks[player.uuid] = scope.launch {
            delay(config.delay)
            sneaks.remove(player.uuid)
        }
    } else {
        when {
            config.crawling && player.isNearGap() -> player.setPose(PlayerPose.Crawling)
            config.sitting -> player.setPose(PlayerPose.Sitting)
        }

        sneaks.remove(player.uuid)?.cancel()
    }
}

private fun ServerPlayerEntity.isNearGap(): Boolean {
    val crawlingDimensions = this.getDimensions(EntityPose.SWIMMING)
    val crouchingDimensions = this.getDimensions(EntityPose.CROUCHING)

    val expectEmptyAt = pos.add(
        Direction.fromRotation(yaw.toDouble()).offsetX * 0.1,
        0.0,
        Direction.fromRotation(yaw.toDouble()).offsetZ * 0.1,
    )
    val expectFullAt = pos.add(
        Direction.fromRotation(yaw.toDouble()).offsetX * 0.1,
        crouchingDimensions.height.toDouble(),
        Direction.fromRotation(yaw.toDouble()).offsetZ * 0.1,
    )

    return world.isSpaceEmpty(this, crawlingDimensions.getBoxAt(expectEmptyAt).contract(1.0e-6)) &&
            !world.isSpaceEmpty(this, crawlingDimensions.getBoxAt(expectFullAt).contract(1.0e-6))
}
