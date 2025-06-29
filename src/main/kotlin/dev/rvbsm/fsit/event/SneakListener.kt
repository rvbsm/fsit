package dev.rvbsm.fsit.event

import dev.rvbsm.fsit.api.event.ClientCommandCallback
import dev.rvbsm.fsit.entity.ModPose
import dev.rvbsm.fsit.entity.RideEntity
import dev.rvbsm.fsit.networking.config
import dev.rvbsm.fsit.networking.lastSneakTime
import dev.rvbsm.fsit.networking.modPose
import dev.rvbsm.fsit.networking.resetLastSneakTime
import dev.rvbsm.fsit.networking.updateLastSneakTime
import net.minecraft.entity.EntityPose
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Util
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

val SneakListener = ClientCommandCallback { player, mode ->
    //? if <=1.21.5 {
    when (mode) {
        ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY -> if (player.firstPassenger is RideEntity) {
            player.removeAllPassengers()
        }

        ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY -> {
            if (player.hasVehicle() || !player.isOnGround) return@ClientCommandCallback
            else if (!player.config.onSneak.sitting && !player.config.onSneak.crawling) return@ClientCommandCallback
            else if (player.pitch < player.config.onSneak.minPitch) return@ClientCommandCallback

            if (Util.getMeasuringTimeMs() - player.lastSneakTime <= player.config.onSneak.delay) {
                player.modPose = when {
                    player.config.onSneak.crawling && player.isNearGap() -> ModPose.Crawling
                    player.config.onSneak.sitting -> ModPose.Sitting
                    else -> return@ClientCommandCallback
                }

                return@ClientCommandCallback player.resetLastSneakTime()
            }

            player.updateLastSneakTime()
        }

        else -> {}
    }
    //?}
}

private fun ServerPlayerEntity.isNearGap(): Boolean {
    val crawlingDimensions = this.getDimensions(EntityPose.SWIMMING)
    val crouchingDimensions = this.getDimensions(EntityPose.CROUCHING)

    val yawRadians = yaw / 180 * PI
    val offsetX = -sin(yawRadians) * 0.1
    val offsetZ = cos(yawRadians) * 0.1

    val expectEmptyAt = pos.add(offsetX, 0.0, offsetZ)
    val expectFullAt = pos.add(offsetX, crouchingDimensions.height.toDouble(), offsetZ)

    return world.isSpaceEmpty(this, crawlingDimensions.getBoxAt(expectEmptyAt).contract(1.0e-6)) &&
        !world.isSpaceEmpty(this, crawlingDimensions.getBoxAt(expectFullAt).contract(1.0e-6))
}
