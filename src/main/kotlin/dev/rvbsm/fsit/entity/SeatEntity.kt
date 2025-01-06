package dev.rvbsm.fsit.entity

import dev.rvbsm.fsit.networking.config
import dev.rvbsm.fsit.networking.realVelocity
import dev.rvbsm.fsit.util.text.literal
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
        get() = Box.of(pos, width.toDouble(), 1.0e-3, width.toDouble())
    private val hasGroundCollision
        get() = world.getCollisions(this, groundCollisionBox).any { !it.isEmpty }

    init {
        calculateDimensions()

        isInvisible = true
        isInvulnerable = true
        isCustomNameVisible = false
        isMarker = true

        customName = "FSit_SeatEntity".literal()
    }

    override fun tick() {
        if (!world.isClient && !isRemoved) {
            super.tickMovement()

            if (!hasPassengers()) {
                discard()
            }

            if (config.sitting.behaviour.shouldDiscard && !hasGroundCollision) {
                detach()
            }

            yaw = player.yaw
        }
    }

    //? if <=1.20.4
    override fun getDimensions(pose: EntityPose): EntityDimensions = player.getDimensions(player.pose)
    //? if >=1.20.5
    /*override fun getBaseDimensions(pose: EntityPose): EntityDimensions = player.getDimensions(player.pose)*/

    //? if <=1.20.1
    override fun getMountedHeightOffset() = 0.0
    //? if >=1.20.2 <=1.20.4
    /*override fun getPassengerAttachmentPos(passenger: net.minecraft.entity.Entity, dimensions: EntityDimensions, scaleFactor: Float): org.joml.Vector3f = Vec3d.ZERO.toVector3f()*/
    //? if >=1.20.5
    /*override fun getPassengerAttachmentPos(passenger: net.minecraft.entity.Entity, dimensions: EntityDimensions, scaleFactor: Float): Vec3d = Vec3d.ZERO*/

    override fun updatePassengerForDismount(passenger: LivingEntity) = findDismountPos(passenger, true)
    override fun getPistonBehavior() = PistonBehavior.NORMAL
    override fun hasPlayerRider() = false
    override fun shouldSave() = false
    override fun hasNoGravity() = !config.sitting.behaviour.shouldMove
    override fun canClip() = !this.hasNoGravity()

    companion object {
        private const val MIN_VELOCITY_LENGTH = 0.3

        fun create(player: ServerPlayerEntity, pos: Vec3d) {
            val seatEntity = SeatEntity(player, pos)

            if (player.config.sitting.behaviour.shouldMove && player.realVelocity.length() >= MIN_VELOCITY_LENGTH) {
//              // todo: applies only after some time and looks comical
                seatEntity.velocity = player.realVelocity
            }

            player.startRiding(seatEntity, true)
            player.world.spawnEntity(seatEntity)
        }
    }
}
