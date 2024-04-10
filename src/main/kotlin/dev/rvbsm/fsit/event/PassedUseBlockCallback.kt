package dev.rvbsm.fsit.event

import dev.rvbsm.fsit.entity.Pose
import dev.rvbsm.fsit.network.getConfig
import dev.rvbsm.fsit.network.setPose
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.block.enums.BlockHalf
import net.minecraft.block.enums.SlabType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.Direction
import net.minecraft.world.World

fun interface PassedUseBlockCallback {
    fun interactBlock(player: ServerPlayerEntity, world: World, hitResult: BlockHitResult): ActionResult

    companion object : PassedUseBlockCallback {
        @JvmField
        val EVENT: Event<PassedUseBlockCallback> =
            EventFactory.createArrayBacked(PassedUseBlockCallback::class.java) { listeners ->
                PassedUseBlockCallback { player, world, hitResult ->
                    for (listener in listeners) {
                        val result = listener.interactBlock(player, world, hitResult)

                        if (result != ActionResult.PASS) {
                            return@PassedUseBlockCallback result
                        }
                    }

                    return@PassedUseBlockCallback ActionResult.PASS
                }
            }

        override fun interactBlock(
            player: ServerPlayerEntity, world: World, hitResult: BlockHitResult
        ): ActionResult {
            return if (hitResult.side != Direction.UP || !world.isAir(hitResult.blockPos.up())) ActionResult.PASS
            else if (player.shouldCancelInteraction()) ActionResult.PASS
            else {
                val config = player.getConfig()
                if (!config.sitting.onUse.enabled) return ActionResult.PASS

                val hitState = world.getBlockState(hitResult.blockPos)

                val isInRange = player.pos.isInRange(hitResult.pos, config.sitting.onUse.range.toDouble())
                val isMaterialMatch = config.sitting.onUse.blocks.any { it.test(hitState) }

                return if (isInRange && isMaterialMatch) {
                    val isSittableSide = when {
                        hitState.contains(Properties.AXIS) -> hitState.get(Properties.AXIS) != Direction.Axis.Y
                        hitState.contains(Properties.BLOCK_HALF) -> hitState.get(Properties.BLOCK_HALF) == BlockHalf.BOTTOM
                        hitState.contains(Properties.SLAB_TYPE) -> hitState.get(Properties.SLAB_TYPE) == SlabType.BOTTOM
                        else -> true
                    }

                    if (isSittableSide) {
                        player.setPose(Pose.Sitting, hitResult.pos)

                        ActionResult.SUCCESS
                    } else ActionResult.PASS
                } else ActionResult.PASS
            }
        }
    }
}
