package dev.rvbsm.fsit.event;

import dev.rvbsm.fsit.FSitMod;
import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FluidModificationItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.stream.Stream;

public abstract class InteractBlockCallback {

	private static final int RADIUS = 2;

	public static ActionResult interactBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
		if (world.isClient) return ActionResult.PASS;
		else if (FSitMod.isModded(player.getUuid())) return ActionResult.PASS;
		else if (!FSitMod.config.sittableSit) return ActionResult.PASS;

		final Item handItem = player.getStackInHand(hand).getItem();
		if (handItem instanceof BlockItem) return ActionResult.PASS;
		else if (handItem instanceof FluidModificationItem) return ActionResult.PASS;
		else if (!player.isOnGround() && player.shouldCancelInteraction()) return ActionResult.PASS;

		if (InteractBlockCallback.isInRadius(player.getPos(), hitResult.getPos()) && InteractBlockCallback.isSittable(world, hitResult)) {
			FSitMod.spawnSeat(player, world, hitResult.getPos());
			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}

	public static boolean isInRadius(Vec3d playerPos, Vec3d sitPos) {
		return playerPos.distanceTo(sitPos) <= RADIUS;
	}

	public static boolean isSittable(World world, BlockHitResult hitResult) {
		if (hitResult.getSide() != Direction.UP) return false;

		final BlockPos blockPos = hitResult.getBlockPos();
		if (!world.isAir(blockPos.up())) return false;

		final BlockState blockState = world.getBlockState(blockPos);
		final Block block = blockState.getBlock();

		final Stream<Identifier> blockTags = blockState.streamTags().map(TagKey::id);
		final Identifier blockIdentifier = Registries.BLOCK.getId(block);
		if (blockTags.anyMatch(FSitMod.config.sittableTags::contains) || FSitMod.config.sittableBlocks.contains(blockIdentifier)) {
			if (block instanceof PillarBlock) {
				return blockState.get(Properties.AXIS) != Direction.Axis.Y;
			} else if (block instanceof StairsBlock) {
				return blockState.get(Properties.BLOCK_HALF) == BlockHalf.BOTTOM;
			} else if (block instanceof SlabBlock) {
				return blockState.get(Properties.SLAB_TYPE) == SlabType.BOTTOM;
			} else return true;
		}

		return false;
	}
}
