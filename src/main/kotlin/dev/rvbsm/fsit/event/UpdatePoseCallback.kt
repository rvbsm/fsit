package dev.rvbsm.fsit.event

import dev.rvbsm.fsit.entity.PlayerPose
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d

fun interface UpdatePoseCallback {
    fun onUpdatePose(player: ServerPlayerEntity, pose: PlayerPose, pos: Vec3d?)

    companion object {
        @JvmField
        val EVENT: Event<UpdatePoseCallback> =
            EventFactory.createArrayBacked(UpdatePoseCallback::class.java) { listeners ->
                UpdatePoseCallback { player, pose, pos ->
                    for (listener in listeners) {
                        listener.onUpdatePose(player, pose, pos)
                    }
                }
            }
    }
}
