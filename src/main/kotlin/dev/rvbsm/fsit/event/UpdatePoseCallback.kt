package dev.rvbsm.fsit.event

import dev.rvbsm.fsit.entity.CrawlEntity
import dev.rvbsm.fsit.entity.Pose
import dev.rvbsm.fsit.entity.SeatEntity
import dev.rvbsm.fsit.network.hasConfig
import dev.rvbsm.fsit.network.hasCrawl
import dev.rvbsm.fsit.network.packet.PoseUpdateS2CPayload
import dev.rvbsm.fsit.network.removeCrawl
import dev.rvbsm.fsit.network.sendIfPossible
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d

fun interface UpdatePoseCallback {
    fun onUpdatePose(player: ServerPlayerEntity, pose: Pose, pos: Vec3d?)

    companion object : UpdatePoseCallback {
        @JvmField
        val EVENT: Event<UpdatePoseCallback> =
            EventFactory.createArrayBacked(UpdatePoseCallback::class.java) { listeners ->
                UpdatePoseCallback { player, pose, pos ->
                    for (listener in listeners) {
                        listener.onUpdatePose(player, pose, pos)
                    }
                }
            }

        override fun onUpdatePose(player: ServerPlayerEntity, pose: Pose, pos: Vec3d?) {
            player.sendIfPossible(PoseUpdateS2CPayload(pose, pos ?: player.pos))

            when (pose) {
                Pose.Standing -> {
                    if (player.vehicle is SeatEntity) player.stopRiding()
                    else if (player.hasCrawl()) player.removeCrawl()
                }

                Pose.Sitting -> {
                    SeatEntity.create(player, pos ?: player.pos)
                }

                Pose.Crawling -> if (!player.hasConfig()) {
                    CrawlEntity.create(player)
                }

                else -> {}
            }
        }
    }
}
