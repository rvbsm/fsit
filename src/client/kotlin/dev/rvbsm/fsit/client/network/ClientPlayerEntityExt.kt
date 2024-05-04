package dev.rvbsm.fsit.client.network

import dev.rvbsm.fsit.api.Poseable
import dev.rvbsm.fsit.entity.PlayerPose
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.util.math.Vec3d

fun ClientPlayerEntity.setPose(pose: PlayerPose, pos: Vec3d? = null) = (this as Poseable).`fsit$setPose`(pose, pos)
fun ClientPlayerEntity.pose(): PlayerPose = (this as Poseable).`fsit$getPose`()
