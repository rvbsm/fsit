package dev.rvbsm.fsit.entity

import dev.rvbsm.fsit.networking.clientVelocity
import dev.rvbsm.fsit.networking.config
import dev.rvbsm.fsit.util.math.times
import dev.rvbsm.fsit.util.text.literal
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.entity.Dismounting
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityPose
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d

class SeatEntity(private val player: ServerPlayerEntity, pos: Vec3d) :
    ArmorStandEntity(player.world, pos.x, pos.y, pos.z) {
    private val config get() = player.config
    private val groundCollisionBox
        get() = Box.of(pos, width.toDouble(), 1.0e-6, width.toDouble())
    private val hasGroundCollision
        get() = world.getCollisions(this, groundCollisionBox).any { !it.isEmpty }

    init {
        calculateDimensions()

        isInvisible = true
        isInvulnerable = true
        isCustomNameVisible = false
        isMarker = true

        customName = "FSit_SeatEntity".literal()

        if (config.sitting.behaviour.shouldMove) {
            velocity = player.clientVelocity * velocityMultiplier.toDouble()
        }
    }

    override fun tick() {
        if (!world.isClient && !isRemoved) {
            super.tickMovement()

            if (firstPassenger == null) {
                discard()
            }

            if (config.sitting.behaviour.shouldDiscard && !hasGroundCollision) {
                detach()
            }

            yaw = player.yaw
        }
    }

    // note: height of the player
    //? if <=1.20.4
    override fun getDimensions(pose: EntityPose): EntityDimensions = player.getDimensions(player.pose)
    //? if >=1.20.5
    /*override fun getBaseDimensions(pose: EntityPose): EntityDimensions = player.getDimensions(player.pose)*/

    //? if <1.20.4
    override fun getMountedHeightOffset() = 0.0
    //? if >=1.20.4 && <1.20.6
    /*override fun getPassengerAttachmentPos(passenger: net.minecraft.entity.Entity, dimensions: EntityDimensions, scaleFactor: Float): org.joml.Vector3f = Vec3d.ZERO.toVector3f()*/
    //? if >=1.20.6
    /*override fun getPassengerAttachmentPos(passenger: net.minecraft.entity.Entity, dimensions: EntityDimensions, scaleFactor: Float): Vec3d = Vec3d.ZERO*/

    /**
     * @see net.minecraft.entity.vehicle.AbstractMinecartEntity.updatePassengerForDismount
     * @see net.minecraft.entity.vehicle.BoatEntity.updatePassengerForDismount
     */
    override fun updatePassengerForDismount(passenger: LivingEntity): Vec3d {
        val dismountOffsets = arrayOf(
            intArrayOf(0, 0),
            *Dismounting.getDismountOffsets(Direction.fromRotation(passenger.yaw.toDouble())),
        )

        for (dismountOffset in dismountOffsets) {
            val dismountBlockPos = BlockPos.ofFloored(x + dismountOffset[0], y, z + dismountOffset[1])
            val dismountHeight = world.getDismountHeight(dismountBlockPos)

            for (passengerPose in passenger.poses) {
                if (Dismounting.canDismountInBlock(dismountHeight)) {
                    val dismountPos = Vec3d.ofCenter(dismountBlockPos, dismountHeight)
                    if (Dismounting.canPlaceEntityAt(world, dismountPos, passenger, passengerPose)) {
                        passenger.pose = passengerPose
                        return dismountPos
                    }
                }
            }
        }

        return pos
    }

    override fun getPistonBehavior() = PistonBehavior.NORMAL
    override fun hasPlayerRider() = false
    override fun shouldSave() = false
    override fun hasNoGravity() = !config.sitting.behaviour.shouldMove
    override fun canClip() = !this.hasNoGravity()

    companion object {
        fun create(player: ServerPlayerEntity, pos: Vec3d) {
            val seatEntity = SeatEntity(player, pos)

            player.startRiding(seatEntity, true)
            player.world.spawnEntity(seatEntity)
        }
    }
}
