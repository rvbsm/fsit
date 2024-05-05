package dev.rvbsm.fsit.entity

import dev.rvbsm.fsit.network.getConfig
import dev.rvbsm.fsit.util.math.addHorizontal
import dev.rvbsm.fsit.util.math.clamp
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.entity.*
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d

class SeatEntity(private val player: ServerPlayerEntity, pos: Vec3d) : Entity(EntityType.BLOCK_DISPLAY, player.world) {
    private val config
        get() = player.getConfig()
    private val groundCollisionBox
        get() = Box.of(pos, width.toDouble(), 1.0e-6, width.toDouble())
    private val onGround
        get() = world.getBlockCollisions(this, groundCollisionBox).any { !it.isEmpty }

    init {
        setPosition(pos)
        calculateDimensions()

        isInvisible = true
        isInvulnerable = true
        isCustomNameVisible = false

        customName = Text.literal("FSit_SeatEntity")

        velocity = player.velocity.addHorizontal(player.horizontalSpeed - player.prevHorizontalSpeed, player.yaw)
    }

    /*? if <=1.20.4 {*/
    override fun initDataTracker() = Unit
    /*?} else {*//*
    override fun initDataTracker(builder: net.minecraft.entity.data.DataTracker.Builder) = Unit
    *//*?} */

    override fun readCustomDataFromNbt(nbt: NbtCompound) = Unit
    override fun writeCustomDataToNbt(nbt: NbtCompound) = Unit

    override fun tick() {
        if (!world.isClient && !isRemoved) {
            if (config.sitting.applyGravity) {
                tickGravity()
            }

            if (firstPassenger == null) {
                discard()
            } else if (!config.sitting.applyGravity && !config.sitting.allowInAir && !onGround) {
                discard()
            }
        }
    }

    private fun tickGravity() {
        updateWaterState()

        velocity = velocity.clamp(0.003)
        velocity = velocity.add(0.0, -0.04, 0.0)
        if (isTouchingWater) {
            velocity = velocity.multiply(0.8, 0.6, 0.8)
        }

        if (isOnGround && velocity.horizontalLengthSquared() > 1.0e-5) {
            val blockSlipperiness = world.getBlockState(velocityAffectingPos).block.slipperiness * 0.9

            velocity = velocity.multiply(blockSlipperiness, 1.0, blockSlipperiness)
        }

        move(MovementType.SELF, velocity)
    }

    override fun removePassenger(passenger: Entity) {
        super.removePassenger(passenger)
    }

    // note: height of the boat
    override fun getDimensions(pose: EntityPose): EntityDimensions = EntityDimensions.fixed(0.6f, 0.5625f)

    override fun updatePassengerForDismount(passenger: LivingEntity): Vec3d = pos

    override fun getPistonBehavior() = PistonBehavior.NORMAL

    override fun hasPlayerRider() = false

    override fun shouldSave() = false

    companion object {
        fun create(player: ServerPlayerEntity, pos: Vec3d) {
            val seatEntity = SeatEntity(player, pos)

            player.startRiding(seatEntity, true)
            player.world.spawnEntity(seatEntity)
        }
    }
}
