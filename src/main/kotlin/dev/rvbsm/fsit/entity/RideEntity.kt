package dev.rvbsm.fsit.entity

import dev.rvbsm.fsit.util.text.literal
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.entity.AreaEffectCloudEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityPose
import net.minecraft.entity.LivingEntity
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import java.util.UUID

class RideEntity(private val player: ServerPlayerEntity) :
    AreaEffectCloudEntity(player.world, player.x, player.y, player.z) {

    init {
        isInvisible = true
        isInvulnerable = true
        isCustomNameVisible = false
        radius = 0f
        duration = Int.MAX_VALUE

        customName = "FSit_RideEntity".literal()
    }

    override fun tick() {
        if (!world.isClient && !isRemoved) {
            if (!hasPassengers()) {
                discard()
            }

            if (vehicle == null) {
                detach()
            }

            yaw = player.yaw
        }
    }

    override fun updatePassengerForDismount(passenger: LivingEntity) = findDismountPos(passenger)
    override fun hasPlayerRider() = false
    override fun shouldSave() = false

    override fun getDimensions(pose: EntityPose): EntityDimensions = EntityDimensions.fixed(0.6f, 1.0e-6f)

    fun isBelongsTo(uuid: UUID) = player.uuid == uuid

    private fun Entity.passengersSequence(): Sequence<Entity> = sequence {
        for (passenger in passengerList) {
            yield(passenger)
            yieldAll(passenger.passengersSequence())
        }
    }

    /**
     * 1.21.2-rc1 added [net.minecraft.entity.EntityType.isSaveable] check that made players non-ridable
     *
     * @see net.minecraft.entity.Entity.startRiding
     */
    fun startRiding(player: ServerPlayerEntity): Boolean {
        if (hasVehicle()) return false

        pose = EntityPose.STANDING
        vehicle = player
        vehicle!!.addPassenger(this)
        val playerPassengers = vehicle!!.passengersSequence().filterIsInstance<ServerPlayerEntity>()

        for (nestedPlayer in playerPassengers) {
            Criteria.STARTED_RIDING.trigger(nestedPlayer)
        }

        return true
    }

    companion object {
        fun create(rider: ServerPlayerEntity, target: ServerPlayerEntity) {
            val rideEntity = RideEntity(rider)

            rider.startRiding(rideEntity, true)
            rider.world.spawnEntity(rideEntity)
            rideEntity.startRiding(target)
            target.networkHandler.sendPacket(EntityPassengersSetS2CPacket(target))
        }
    }
}
