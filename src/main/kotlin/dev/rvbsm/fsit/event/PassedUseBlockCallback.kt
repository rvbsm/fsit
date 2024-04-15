package dev.rvbsm.fsit.event

import dev.rvbsm.fsit.entity.Pose
import dev.rvbsm.fsit.network.getConfig
import dev.rvbsm.fsit.network.setPose
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.block.BlockState
import net.minecraft.block.enums.BlockHalf
import net.minecraft.block.enums.SlabType
import net.minecraft.entity.Entity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShapes
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

        override fun interactBlock(player: ServerPlayerEntity, world: World, hitResult: BlockHitResult): ActionResult {
            if (player.shouldCancelInteraction() || hitResult.side != Direction.UP) return ActionResult.PASS

            val onUseConfig = player.getConfig().sitting.onUse
            val hitState = world.getBlockState(hitResult.blockPos)

            if (!onUseConfig.enabled ||
                !player.pos.isInRange(hitResult.pos, onUseConfig.range.toDouble()) ||
                !hitState.isSittableSide() ||
                !onUseConfig.blocks.any { it.test(hitState) } ||
                world.willSuffocate(player, hitResult.pos)
            ) return ActionResult.PASS

            player.setPose(Pose.Sitting, hitResult.pos)
            return ActionResult.SUCCESS
        }

        // todo: configurable block properties?
        private fun BlockState.isSittableSide() = when {
            contains(Properties.AXIS) -> get(Properties.AXIS) != Direction.Axis.Y
            contains(Properties.BLOCK_HALF) -> get(Properties.BLOCK_HALF) == BlockHalf.BOTTOM
            contains(Properties.SLAB_TYPE) -> get(Properties.SLAB_TYPE) == SlabType.BOTTOM
            else -> true
        }

        /** adapted from [net.minecraft.entity.Entity.isInsideWall] */
        private fun World.willSuffocate(entity: Entity, pos: Vec3d): Boolean {
            val box = Box.of(
                pos.add(0.0, entity.standingEyeHeight.toDouble() - 0.5, 0.0),
                entity.width.toDouble(), 1.0e-6, entity.width.toDouble(),
            )

            return BlockPos.stream(box).anyMatch isInsideBlock@{
                val blockState = getBlockState(it)

                !blockState.isAir && blockState.shouldSuffocate(this, it) && VoxelShapes.matchesAnywhere(
                    blockState.getCollisionShape(this, it).offset(it.x.toDouble(), it.y.toDouble(), it.z.toDouble()),
                    VoxelShapes.cuboid(box),
                    BooleanBiFunction.AND,
                )
            }
        }
    }
}
