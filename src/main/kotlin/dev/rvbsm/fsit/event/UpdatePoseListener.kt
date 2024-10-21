package dev.rvbsm.fsit.event

import dev.rvbsm.fsit.entity.CrawlEntity
import dev.rvbsm.fsit.entity.PlayerPose
import dev.rvbsm.fsit.entity.SeatEntity
import dev.rvbsm.fsit.networking.config
import dev.rvbsm.fsit.networking.hasConfig
import dev.rvbsm.fsit.networking.hasCrawl
import dev.rvbsm.fsit.networking.payload.PoseUpdateS2CPayload
import dev.rvbsm.fsit.networking.removeCrawl
import dev.rvbsm.fsit.networking.resetPose
import dev.rvbsm.fsit.networking.trySend
import dev.rvbsm.fsit.util.math.centered

val UpdatePoseListener = UpdatePoseCallback update@{ player, pose, pos ->
    when (pose) {
        PlayerPose.Standing -> {
            if (player.vehicle is SeatEntity) player.stopRiding()
            else if (player.hasCrawl()) player.removeCrawl()
        }

        PlayerPose.Sitting -> {
            // note: prevents from creating seats in the air without gravity
            if (!player.config.sitting.behaviour.shouldMove && !player.isOnGround) {
                return@update player.resetPose()
            }
            val seatPos = (pos ?: player.pos).let {
                if (player.config.sitting.shouldCenter) it.centered()
                else it
            }

            SeatEntity.create(player, seatPos)
        }

        PlayerPose.Crawling -> if (!player.hasConfig()) {
            CrawlEntity.create(player)
        }

        else -> {}
    }

    player.trySend(PoseUpdateS2CPayload(pose, pos ?: player.pos))
}
