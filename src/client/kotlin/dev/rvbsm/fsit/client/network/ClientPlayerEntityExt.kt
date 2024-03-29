package dev.rvbsm.fsit.client.network

import dev.rvbsm.fsit.api.Poseable
import dev.rvbsm.fsit.entity.Pose
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.util.math.Vec3d

fun ClientPlayerEntity.setPose(pose: Pose, pos: Vec3d? = null) = (this as Poseable).`fsit$setPose`(pose, pos)
fun ClientPlayerEntity.pose(): Pose = (this as Poseable).`fsit$getPose`()
