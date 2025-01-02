package dev.rvbsm.fsit.event

import dev.rvbsm.fsit.api.event.UpdatePoseCallback
import dev.rvbsm.fsit.entity.CrawlEntity
import dev.rvbsm.fsit.entity.ModPose
import dev.rvbsm.fsit.entity.SeatEntity
import dev.rvbsm.fsit.networking.config
import dev.rvbsm.fsit.networking.hasConfig
import dev.rvbsm.fsit.networking.hasCrawl
import dev.rvbsm.fsit.networking.payload.PoseUpdateS2CPayload
import dev.rvbsm.fsit.networking.removeCrawl
import dev.rvbsm.fsit.networking.resetPose
import dev.rvbsm.fsit.networking.trySend
import dev.rvbsm.fsit.util.math.centered
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.BlockView

val UpdatePoseListener = UpdatePoseCallback update@{ player, pose, pos ->
    if (pose != ModPose.Standing && player.isSneaking) {
        return@update player.resetPose()
    }

    when (pose) {
        ModPose.Standing -> {
            if (player.vehicle is SeatEntity) player.stopRiding()
            else if (player.hasCrawl()) player.removeCrawl()
        }

        ModPose.Sitting -> {
            // note: prevents from creating seats in the air without gravity
            if (!player.config.sitting.behaviour.shouldMove && !player.isOnGround) {
                return@update player.resetPose()
            }
            val seatPos = (pos ?: player.pos).let {
                if (player.config.sitting.shouldCenter) it.centered()
                else it
            }.optimizeHeight(player.world)

            SeatEntity.create(player, seatPos)
        }

        ModPose.Crawling -> if (!player.hasConfig()) {
            CrawlEntity.create(player)
        }

        else -> {}
    }

    player.trySend(PoseUpdateS2CPayload(pose, pos ?: player.pos))
}

/**
 * spamming sit creation buries player :skull:
 */
private fun Vec3d.optimizeHeight(world: BlockView): Vec3d {
    val blockPos = BlockPos.ofFloored(this)
    val blockState = world.getBlockState(blockPos)
    val blockCollisions = blockState.getCollisionShape(world, blockPos)
    val blockHeight = blockCollisions.getMax(Direction.Axis.Y).coerceIn(1.0, 2.0)

    return add(0.0, blockHeight - 1.0, 0.0)
}
