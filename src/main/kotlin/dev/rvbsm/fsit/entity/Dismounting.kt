package dev.rvbsm.fsit.entity

import dev.rvbsm.fsit.util.math.plus
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape

/** @see net.minecraft.entity.vehicle.BoatEntity.updatePassengerForDismount */
internal fun Entity.findDismountPos(passenger: LivingEntity, checkVehiclePosition: Boolean = false): Vec3d {
    val dismountSequence = sequence<Vec3d> {
        if (checkVehiclePosition) {
            yield(pos)
        }

        val vehicleWidth = width.toDouble() * MathHelper.SQUARE_ROOT_OF_TWO
        val passengerWidth = passenger.width.toDouble()
        val passengerYaw = passenger.yaw

        val dismountOffset = Entity.getPassengerDismountOffset(vehicleWidth, passengerWidth, passengerYaw)
        val dismountPos = pos + dismountOffset

        yield(dismountPos)
        yield(dismountPos.subtract(0.0, 1.0, 0.0))
    }.mapNotNull { dismountPos ->
        val dismountHeight = world.getDismountHeight(BlockPos.ofFloored(dismountPos)).takeIf { it.isFinite() && it < 1 }

        dismountHeight?.let {
            dismountPos.add(0.0, dismountHeight, 0.0)
        }
    }

    for (dismountPos in dismountSequence) {
        val dismountPose = passenger.poses.find { passengerPose ->
            val poseBox = passenger.getBoundingBox(passengerPose).offset(dismountPos)
            world.getCollisions(passenger, poseBox).all(VoxelShape::isEmpty) && poseBox in world.worldBorder
        } ?: continue

        passenger.pose = dismountPose
        return dismountPos
    }

    return pos
}
