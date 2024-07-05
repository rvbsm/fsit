package dev.rvbsm.fsit.entity

import dev.rvbsm.fsit.util.literal
import net.minecraft.entity.AreaEffectCloudEntity
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityPose
import net.minecraft.entity.LivingEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d
import java.util.*

class RideEntity(private val player: ServerPlayerEntity) :
    AreaEffectCloudEntity(player.world, player.pos.x, player.pos.y, player.pos.z) {
    init {
        isInvisible = true
        isInvulnerable = true
        isCustomNameVisible = false
        radius = 0f
        duration = Int.MAX_VALUE

        customName = "FSit_RideEntity".literal()
    }

    override fun tick() {
        if (firstPassenger == null || vehicle == null) {
            discard()
        }
    }

    override fun updatePassengerForDismount(passenger: LivingEntity): Vec3d = vehicle?.pos ?: pos
    override fun hasPlayerRider() = false
    override fun shouldSave() = false

    override fun getDimensions(pose: EntityPose): EntityDimensions = EntityDimensions.fixed(0.6f, 1.0e-6f)

    fun isBelongsTo(uuid: UUID) = player.uuid == uuid

    companion object {
        fun create(rider: ServerPlayerEntity, target: ServerPlayerEntity) {
            val rideEntity = RideEntity(rider)

            rider.startRiding(rideEntity, true)
            rider.world.spawnEntity(rideEntity)
            rideEntity.startRiding(target)
        }
    }
}
