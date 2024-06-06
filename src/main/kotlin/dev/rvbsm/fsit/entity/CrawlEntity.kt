package dev.rvbsm.fsit.entity

import dev.rvbsm.fsit.network.setCrawl
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

// todo: hide shulker's head somehow 😥
class CrawlEntity(private val player: ServerPlayerEntity) : Entity(EntityType.SHULKER, player.world) {
    private val crawlPos
        get() = player.blockPos.toCenterPos()

    init {
        setPosition(crawlPos)

        isSilent = true
        isInvisible = true
        isInvulnerable = true
        isCustomNameVisible = false

        customName = Text.literal("FSit_CrawlEntity")
    }

    //? if <=1.20.4
    override fun initDataTracker() = Unit
    //? if >1.20.4
    /*override fun initDataTracker(builder: net.minecraft.entity.data.DataTracker.Builder) = Unit*/


    override fun readCustomDataFromNbt(nbt: NbtCompound) = Unit
    override fun writeCustomDataToNbt(nbt: NbtCompound) = Unit

    override fun remove(reason: RemovalReason) {
        player.networkHandler.sendPacket(EntitiesDestroyS2CPacket(id))
    }

    override fun tick() {
        if (blockPos != player.blockPos.up() || age % 10 == 0) {
            setPosition(crawlPos)
            player.networkHandler.sendPacket(EntityPositionS2CPacket(this))
        }
    }

    companion object {
        fun create(player: ServerPlayerEntity) {
            val crawlEntity = CrawlEntity(player)

            player.networkHandler.sendPacket(EntitySpawnS2CPacket(crawlEntity, 0, crawlEntity.blockPos))
            player.networkHandler.sendPacket(
                EntityTrackerUpdateS2CPacket(
                    crawlEntity.id, crawlEntity.dataTracker.dirtyEntries
                )
            )

            player.setCrawl(crawlEntity)
        }
    }
}
