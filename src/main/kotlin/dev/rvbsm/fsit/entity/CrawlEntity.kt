package dev.rvbsm.fsit.entity

import dev.rvbsm.fsit.networking.setCrawl
import dev.rvbsm.fsit.util.literal
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d

// todo: hide shulker's head somehow 😥
class CrawlEntity(private val player: ServerPlayerEntity) : Entity(EntityType.SHULKER, player.world) {
    private val crawlBlockPos get() = player.blockPos.up()
    private val crawlPos get() = Vec3d.ofBottomCenter(crawlBlockPos)

    init {
        setPosition(crawlPos)

        isSilent = true
        isInvisible = true
        isInvulnerable = true
        isCustomNameVisible = false

        customName = "FSit_CrawlEntity".literal()
    }

    //? if <=1.20.4
    override fun initDataTracker() = Unit
    //? if >=1.20.5
    /*override fun initDataTracker(builder: net.minecraft.entity.data.DataTracker.Builder) = Unit*/

    override fun readCustomDataFromNbt(nbt: NbtCompound) = Unit
    override fun writeCustomDataToNbt(nbt: NbtCompound) = Unit

    override fun remove(reason: RemovalReason) {
        player.networkHandler.sendPacket(EntitiesDestroyS2CPacket(id))
    }

    override fun tick() {
        if (blockPos != crawlBlockPos && age % 10 == 0) {
            setPosition(crawlPos)
            player.networkHandler.sendPacket(EntityPositionS2CPacket(this))
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
