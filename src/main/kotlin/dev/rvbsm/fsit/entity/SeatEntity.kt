package dev.rvbsm.fsit.entity

import dev.rvbsm.fsit.network.clientVelocity
import dev.rvbsm.fsit.network.config
import dev.rvbsm.fsit.util.literal
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityPose
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d

class SeatEntity(private val player: ServerPlayerEntity, pos: Vec3d) :
    ArmorStandEntity(player.world, pos.x, pos.y, pos.z) {
    private val config get() = player.config
    private val groundCollisionBox
        get() = Box.of(pos, width.toDouble(), 1.0e-6, width.toDouble())
    private val hasGroundCollision
        get() = world.getBlockCollisions(this, groundCollisionBox).any { !it.isEmpty }

    init {
        calculateDimensions()

        isInvisible = true
        isInvulnerable = true
        isCustomNameVisible = false
        isMarker = true

        customName = "FSit_SeatEntity".literal()

        if (config.sitting.applyGravity) {
            velocity = player.clientVelocity
        }
    }

    override fun tick() {
        if (!world.isClient && !isRemoved) {
            super.tickMovement()

            if (firstPassenger == null || (!config.sitting.allowInAir && hasNoGravity() && !hasGroundCollision)) {
                discard()
            }
        }
    }

    // note: height of the player
    //? if <1.20.1 && <1.20.6
    override fun getDimensions(pose: EntityPose): EntityDimensions = player.getDimensions(player.pose)
    //? if >=1.20.6
    /*override fun getBaseDimensions(pose: EntityPose): EntityDimensions = player.getDimensions(player.pose)*/

    //? if >=1.20.1 && <1.20.4
    override fun getMountedHeightOffset() = 0.0
    //? if >=1.20.4 && <1.20.6
    /*override fun getPassengerAttachmentPos(passenger: Entity, dimensions: EntityDimensions, scaleFactor: Float): Vector3f = Vec3d.ZERO.toVector3f()*/
    //? if >=1.20.6
    /*override fun getPassengerAttachmentPos(passenger: Entity, dimensions: EntityDimensions, scaleFactor: Float): Vec3d = Vec3d.ZERO*/

    override fun updatePassengerForDismount(passenger: LivingEntity): Vec3d = pos
    override fun getPistonBehavior() = PistonBehavior.NORMAL
    override fun hasPlayerRider() = false
    override fun shouldSave() = false
    override fun hasNoGravity() = !config.sitting.applyGravity
    override fun canClip() = !this.hasNoGravity()

    companion object {
        fun create(player: ServerPlayerEntity, pos: Vec3d) {
            val seatEntity = SeatEntity(player, pos)

            player.startRiding(seatEntity, true)
            player.world.spawnEntity(seatEntity)
        }
    }
}
