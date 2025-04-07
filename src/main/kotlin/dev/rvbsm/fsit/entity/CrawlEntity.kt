package dev.rvbsm.fsit.entity

import dev.rvbsm.fsit.networking.setCrawl
import dev.rvbsm.fsit.util.text.literal
import net.minecraft.block.Blocks
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
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import kotlin.math.roundToInt

class CrawlEntity(private val player: ServerPlayerEntity) : ShulkerEntity(EntityType.SHULKER, player.world) {
    private var isVisible = false

    //? if <=1.21.1
    private var prevBlockPos = BlockPos.ORIGIN

    // todo: imagine someone bumps into it
    private var prevBlockState = Blocks.STRUCTURE_VOID.defaultState
    private var prevPlayerPos = Vec3d.ZERO

    init {
        setPosition(player.pos)
        this.dataTracker.set(ATTACHED_FACE, Direction.UP)

        isSilent = true
        isInvisible = true
        isInvulnerable = true
        isCustomNameVisible = false

        customName = "FSit_CrawlEntity".literal()
    }

    override fun tick() {
        if (age % 4 == 0) {
            val blockState = world.getBlockState(blockPos)
            if (this.prevPlayerPos == player.pos && this.prevBlockState == blockState) {
                return
            }

            this.prevPlayerPos = player.pos
            this.prevBlockState = blockState
            this.prevBlockPos = blockPos
            this.setPosition(this.player.pos.add(0.0, 1.49, 0.0))

            val bundle = mutableSetOf<Packet<ClientPlayPacketListener>>()

            // why shulkers' peek uses bytes?
            val deltaHeight = ((if (player.y < 0) player.y % 1 + 1 else player.y % 1) * 100).roundToInt()

            //? if <=1.21.1 {
            if (blockState.isSideSolidFullSquare(world, blockPos, Direction.DOWN)) {
                bundle.add(prevBlockPos.createUpdatePacket())
                bundle.add(blockPos.createUpdatePacket())
                bundle.add(EntitiesDestroyS2CPacket(id))

                isVisible = false
            } else if (deltaHeight < 49 && blockState.isAir && player.isOnGround) {
                if (isVisible) {
                    bundle.add(EntitiesDestroyS2CPacket(id))
                    isVisible = false
                }

                bundle.add(prevBlockPos.createUpdatePacket())
                bundle.add(blockPos.createUpdatePacket(Blocks.BARRIER.defaultState))
            } else {
            //?}
                if (!isVisible) {
                    //? if <=1.21.1
                    bundle.add(prevBlockPos.createUpdatePacket())
                    bundle.add(EntitySpawnS2CPacket(this, 0, blockPos))

                    isVisible = true
                }

                this.dataTracker.set(PEEK_AMOUNT, if (deltaHeight < 49) 0 else (100 - deltaHeight).toByte())

                bundle.add(EntityTrackerUpdateS2CPacket(id, dataTracker.changedEntries ?: listOf()))
                bundle.add(//$ EntityPositionSyncS2CPacket.create >>
                    net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket(this)
                )
            //? if <=1.21.1
            }

            player.networkHandler.sendPacket(BundleS2CPacket(bundle))
        }
    }

    override fun remove(reason: RemovalReason) {
        if (isVisible) player.networkHandler.sendPacket(EntitiesDestroyS2CPacket(id))
        //? if <=1.21.1
        else player.networkHandler.sendPacket(blockPos.createUpdatePacket())
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
