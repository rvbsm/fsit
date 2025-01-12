package dev.rvbsm.fsit.event

import dev.rvbsm.fsit.api.event.PassedUseBlockCallback
import dev.rvbsm.fsit.entity.ModPose
import dev.rvbsm.fsit.networking.config
import dev.rvbsm.fsit.networking.setPose
import dev.rvbsm.fsit.registry.RegistrySet
import net.minecraft.block.Block
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

val SpawnSeatListener = PassedUseBlockCallback interact@{ player, _, hitResult ->
    if (player.shouldCancelInteraction() || hitResult.side != Direction.UP) return@interact ActionResult.PASS
    if (!player.canSitOn(hitResult)) return@interact ActionResult.PASS

    player.setPose(ModPose.Sitting, hitResult.pos)

    //? if <=1.21.1
    return@interact ActionResult.SUCCESS
    //? if >=1.21.2
    /*return@interact ActionResult.SUCCESS_SERVER*/
}

private fun ServerPlayerEntity.canSitOn(hitResult: BlockHitResult) =
    if (!pos.isInRange(hitResult.pos, config.onUse.range.toDouble())) false
    else if (!config.onUse.sitting) false
    else if (config.onUse.checkSuffocation && world.willBlockVision(this, hitResult.pos)) false
    else world.getBlockState(hitResult.blockPos).let { hitState ->
        hitState.isSittableSide() && config.onUse.blocks.test(hitState)
    }

// todo: configurable block properties?
private fun BlockState.isSittableSide() = when {
    contains(Properties.AXIS) -> get(Properties.AXIS) != Direction.Axis.Y
    contains(Properties.BLOCK_HALF) -> get(Properties.BLOCK_HALF) == BlockHalf.BOTTOM
    contains(Properties.SLAB_TYPE) -> get(Properties.SLAB_TYPE) == SlabType.BOTTOM
    else -> true
}

/** adapted from [net.minecraft.entity.Entity.isInsideWall] */
private fun World.willBlockVision(entity: Entity, pos: Vec3d): Boolean {
    val eyePos = pos.add(0.0, entity.standingEyeHeight - 0.5, 0.0)
    val width = entity.width * 0.8
    val box = Box.of(eyePos, width, 1.0e-6, width)

    return BlockPos.stream(box).anyMatch {
        val blockState = getBlockState(it)

        !blockState.isAir && blockState.shouldBlockVision(this, it) && VoxelShapes.matchesAnywhere(
            blockState.getCollisionShape(this, it).offset(it.x.toDouble(), it.y.toDouble(), it.z.toDouble()),
            VoxelShapes.cuboid(box),
            BooleanBiFunction.AND,
        )
    }
}

private fun RegistrySet<Block>.test(state: BlockState) =
    entries.any { (_, block) -> state.isOf(block) } || tags.any { (_, tag) -> state.isIn(tag) }
