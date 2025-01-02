package dev.rvbsm.fsit.entity

import dev.rvbsm.fsit.util.math.plus
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape

/** @see net.minecraft.entity.vehicle.BoatEntity.updatePassengerForDismount */
fun getDismountPosition(vehicle: Entity, passenger: LivingEntity): Vec3d {
    val world = vehicle.world

    val dismountSequence = sequence<Vec3d> {
        val vehicleDismountHeight = world.getDismountHeight(vehicle.blockPos)
        if (vehicleDismountHeight.isFinite() && vehicleDismountHeight < 1) {
            yield(vehicle.pos)
        }

        val dismountOffset = Entity.getPassengerDismountOffset(
            vehicle.width.toDouble() * MathHelper.SQUARE_ROOT_OF_TWO,
            passenger.width.toDouble(),
            passenger.yaw,
        )

        var dismountBlockPos = BlockPos.ofFloored(vehicle.pos + dismountOffset)
        repeat(2) {
            val dismountHeight = world.getDismountHeight(dismountBlockPos)
            if (dismountHeight.isFinite() && dismountHeight < 1) {
                yield(Vec3d.add(dismountBlockPos, 0.5, dismountHeight, 0.5))
            }

            dismountBlockPos = dismountBlockPos.down()
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

    return vehicle.pos
}
