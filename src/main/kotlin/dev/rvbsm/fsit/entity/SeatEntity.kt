package dev.rvbsm.fsit.entity

import dev.rvbsm.fsit.network.getConfig
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.entity.*
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d

class SeatEntity(private val player: ServerPlayerEntity, pos: Vec3d) : Entity(EntityType.BLOCK_DISPLAY, player.world) {
    init {
        setPosition(pos)
        velocity = player.velocity
        calculateDimensions()

        isInvisible = true
        isInvulnerable = true
        isCustomNameVisible = false

        customName = Text.literal("FSit_SeatEntity")
    }

    /*? if >=1.20.5- {*//*
    override fun initDataTracker(builder: net.minecraft.entity.data.DataTracker.Builder) = Unit
    *//*?} else {*/
    override fun initDataTracker() = Unit
    /*?} */

    override fun readCustomDataFromNbt(nbt: NbtCompound) = Unit

    override fun writeCustomDataToNbt(nbt: NbtCompound) = Unit

    override fun tick() {
        if (!world.isClient && !isRemoved) {
            val config = player.getConfig().sitting
            if (config.applyGravity) {
                velocity = velocity.add(0.0, -0.04, 0.0)
                move(MovementType.SELF, velocity)
            } else if (!config.allowInAir) {
                val box = Box.of(pos, 1.0e-6, 1.0e-6, 1.0e-6)
                if (world.isSpaceEmpty(this, box)) {
                    discard()
                }
            }

            if (firstPassenger == null) {
                discard()
            }
        }
    }

    // note: height of the boat
    override fun getDimensions(pose: EntityPose): EntityDimensions = EntityDimensions.fixed(0.6f, 0.5625f)

    override fun updatePassengerForDismount(passenger: LivingEntity): Vec3d = pos

    override fun getPistonBehavior() = PistonBehavior.NORMAL

    override fun hasPlayerRider() = false

    override fun shouldSave() = false

    companion object {
        fun create(player: ServerPlayerEntity, pos: Vec3d) {
            val seat = SeatEntity(player, pos)

            player.startRiding(seat, true)
            player.world.spawnEntity(seat)
        }
    }
}
