package dev.rvbsm.fsit.entity

import dev.rvbsm.fsit.networking.setCrawl
import dev.rvbsm.fsit.util.text.literal
import net.minecraft.entity.EntityType
import net.minecraft.entity.mob.ShulkerEntity
import net.minecraft.network.packet.s2c.play.BundleS2CPacket
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d

// todo: hide shulker's head somehow 😥
class CrawlEntity(private val player: ServerPlayerEntity) : ShulkerEntity(EntityType.SHULKER, player.world) {
    private val crawlBlockPos get() = player.blockPos.up()
    private val crawlPos get() = Vec3d.ofBottomCenter(crawlBlockPos)
    private val entityDestroyPayload = EntitiesDestroyS2CPacket(id)
    private val positionSyncPayload get() =
        //? if <=1.21.1
        net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket(this)
        //? if >=1.21.2-alpha.0
        /*net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket.create(this)*/

    init {
        setPosition(crawlPos)

        isSilent = true
        isInvisible = true
        isInvulnerable = true
        isCustomNameVisible = false

        customName = "FSit_CrawlEntity".literal()
    }

    override fun remove(reason: RemovalReason) {
        player.networkHandler.sendPacket(entityDestroyPayload)
    }

    override fun tick() {
        if (blockPos != crawlBlockPos && age % 10 == 0) {
            setPosition(crawlPos)
            player.networkHandler.sendPacket(positionSyncPayload)
        }
    }

    companion object {
        fun create(player: ServerPlayerEntity) {
            val crawlEntity = CrawlEntity(player)

            player.networkHandler.sendPacket(
                BundleS2CPacket(
                    setOf(
                        EntitySpawnS2CPacket(crawlEntity, 0, player.blockPos.up()),
                        EntityTrackerUpdateS2CPacket(crawlEntity.id, crawlEntity.dataTracker.dirtyEntries ?: listOf()),
                    )
                )
            )

            player.setCrawl(crawlEntity)
        }
    }
}
