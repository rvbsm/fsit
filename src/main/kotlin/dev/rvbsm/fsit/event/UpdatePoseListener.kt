package dev.rvbsm.fsit.event

import dev.rvbsm.fsit.entity.CrawlEntity
import dev.rvbsm.fsit.entity.PlayerPose
import dev.rvbsm.fsit.entity.SeatEntity
import dev.rvbsm.fsit.networking.*
import dev.rvbsm.fsit.networking.payload.PoseUpdateS2CPayload

val UpdatePoseListener = UpdatePoseCallback update@{ player, pose, pos ->
    when (pose) {
        PlayerPose.Standing -> {
            if (player.vehicle is SeatEntity) player.stopRiding()
            else if (player.hasCrawl()) player.removeCrawl()
        }

        PlayerPose.Sitting -> {
            if (player.config.sitting.behaviour.shouldDiscard && !player.isOnGround) {
                return@update player.setPose(PlayerPose.Standing, pos)
            }

            SeatEntity.create(player, pos ?: player.pos)
        }

        PlayerPose.Crawling -> if (!player.hasConfig()) {
            CrawlEntity.create(player)
        }

        else -> {}
    }

    player.trySend(PoseUpdateS2CPayload(pose, pos ?: player.pos))
}
