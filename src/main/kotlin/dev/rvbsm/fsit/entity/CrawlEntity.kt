package dev.rvbsm.fsit.entity

import dev.rvbsm.fsit.networking.setCrawl
import dev.rvbsm.fsit.util.text.literal
import net.minecraft.entity.EntityType
import net.minecraft.entity.mob.ShulkerEntity
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BundleS2CPacket
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class CrawlEntity(private val player: ServerPlayerEntity) : ShulkerEntity(EntityType.SHULKER, player.world) {
    private val crawlBlockPos get() = player.blockPos.up()
    private val crawlPos get() = Vec3d.ofBottomCenter(crawlBlockPos)
    private var isVisible = false
    private var prevBlockPos = BlockPos.ORIGIN

    init {
        setPosition(player.pos)

        isSilent = true
        isInvisible = true
        isInvulnerable = true
        isCustomNameVisible = false

        customName = "FSit_CrawlEntity".literal()
    }

    override fun tick() {
        if (age % 20 == 0 || (age % 10 == 0 && blockPos != crawlBlockPos)) {
            prevBlockPos = blockPos
            setPosition(crawlPos)

            val bundle = mutableSetOf<Packet<ClientPlayPacketListener>>()

            //? if <=1.21.1 {
            val blockState = world.getBlockState(blockPos)
            if (blockState.isSideSolidFullSquare(world, blockPos, net.minecraft.util.math.Direction.DOWN)) {
                bundle.add(prevBlockPos.createUpdatePacket())
                bundle.add(blockPos.createUpdatePacket())
                bundle.add(EntitiesDestroyS2CPacket(id))
                isVisible = false
            } else if (!blockState.isAir || !player.isOnGround) {
                if (!isVisible) {
                    bundle.add(prevBlockPos.createUpdatePacket())
                    bundle.add(EntitySpawnS2CPacket(this, 0, blockPos))
                    bundle.add(EntityTrackerUpdateS2CPacket(id, dataTracker.changedEntries ?: listOf()))
                    isVisible = true
                }

                bundle.add(net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket(this))
            } else {
                if (isVisible) {
                    bundle.add(EntitiesDestroyS2CPacket(id))
                    isVisible = false
                }

                bundle.add(prevBlockPos.createUpdatePacket())
                bundle.add(blockPos.createUpdatePacket(net.minecraft.block.Blocks.BARRIER.defaultState))
            }
            //?} else if >=1.21.2 {
            /*if (!isVisible) {
                bundle.add(EntitySpawnS2CPacket(this, 0, blockPos))
                bundle.add(EntityTrackerUpdateS2CPacket(id, dataTracker.changedEntries ?: listOf()))
                isVisible = true
            }

            bundle.add(net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket.create(this))
            *///?}

            player.networkHandler.sendPacket(BundleS2CPacket(bundle))
        }
    }

    override fun remove(reason: RemovalReason) {
        //? if <=1.21.1 {
        if (isVisible) {
            player.networkHandler.sendPacket(EntitiesDestroyS2CPacket(id))
        } else {
            player.networkHandler.sendPacket(blockPos.createUpdatePacket())
        }
        //?} else if >=1.21.1
        /*player.networkHandler.sendPacket(EntitiesDestroyS2CPacket(id))*/
    }

    //? if <=1.21.1 {
    private fun BlockPos.createUpdatePacket(state: net.minecraft.block.BlockState = world.getBlockState(this)) =
        net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket(this, state)
    //?}

    companion object {
        fun create(player: ServerPlayerEntity) {
            val crawlEntity = CrawlEntity(player)
            player.setCrawl(crawlEntity)
        }
    }
}
