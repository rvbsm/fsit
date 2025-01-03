package dev.rvbsm.fsit.event

import dev.rvbsm.fsit.api.event.ClientCommandCallback
import dev.rvbsm.fsit.entity.ModPose
import dev.rvbsm.fsit.networking.config
import dev.rvbsm.fsit.networking.lastSneakTime
import dev.rvbsm.fsit.networking.modPose
import dev.rvbsm.fsit.util.math.toHorizontalDirection
import net.minecraft.entity.EntityPose
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Util

val SneakListener = ClientCommandCallback { player, mode ->
    if (mode == ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY) {
        if (player.hasVehicle() || !player.isOnGround) return@ClientCommandCallback
        else if (!player.config.onSneak.sitting && !player.config.onSneak.crawling) return@ClientCommandCallback
        else if (player.pitch < player.config.onSneak.minPitch) return@ClientCommandCallback

        if (Util.getMeasuringTimeMs() - player.lastSneakTime <= player.config.onSneak.delay) when {
            player.config.onSneak.crawling && player.isNearGap() -> player.modPose = ModPose.Crawling
            player.config.onSneak.sitting -> player.modPose = ModPose.Sitting
        }
    }
}

private fun ServerPlayerEntity.isNearGap(): Boolean {
    val crawlingDimensions = this.getDimensions(EntityPose.SWIMMING)
    val crouchingDimensions = this.getDimensions(EntityPose.CROUCHING)

    val expectEmptyAt = pos.add(
        yaw.toHorizontalDirection().offsetX * 0.1,
        0.0,
        yaw.toHorizontalDirection().offsetZ * 0.1,
    )
    val expectFullAt = pos.add(
        yaw.toHorizontalDirection().offsetX * 0.1,
        crouchingDimensions.height.toDouble(),
        yaw.toHorizontalDirection().offsetZ * 0.1,
    )

    return world.isSpaceEmpty(this, crawlingDimensions.getBoxAt(expectEmptyAt).contract(1.0e-6)) &&
            !world.isSpaceEmpty(this, crawlingDimensions.getBoxAt(expectFullAt).contract(1.0e-6))
}
