package dev.rvbsm.fsit.entity

import dev.rvbsm.fsit.network.setCrawl
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

// todo: hide shulker's head somehow 😥
class CrawlEntity(private val player: ServerPlayerEntity) : Entity(EntityType.SHULKER, player.world) {
    private val crawlPos
        get() = player.pos.add(0.0, 1.0, 0.0)

    init {
        setPosition(crawlPos)

        isSilent = true
        isInvisible = true
        isInvulnerable = true
        isCustomNameVisible = false

        customName = Text.literal("FSit_CrawlEntity")
    }

    override fun initDataTracker() = Unit

    override fun readCustomDataFromNbt(nbt: NbtCompound) = Unit

    override fun writeCustomDataToNbt(nbt: NbtCompound) = Unit

    override fun remove(reason: RemovalReason) {
        player.networkHandler.sendPacket(EntitiesDestroyS2CPacket(id))
    }

    override fun tick() {
        if (blockPos.getManhattanDistance(player.blockPos) > 1) {
            setPosition(crawlPos)
            player.networkHandler.sendPacket(EntityPositionS2CPacket(this))
        }
    }

    companion object {
        fun create(player: ServerPlayerEntity) {
            val crawlEntity = CrawlEntity(player)

            player.networkHandler.sendPacket(crawlEntity.createSpawnPacket())
            player.networkHandler.sendPacket(
                EntityTrackerUpdateS2CPacket(
                    crawlEntity.id, crawlEntity.dataTracker.dirtyEntries
                )
            )

            player.setCrawl(crawlEntity)
        }
    }
}
