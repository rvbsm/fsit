package dev.rvbsm.fsit.event;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.config.FSitConfig;
import dev.rvbsm.fsit.entity.SeatEntity;
import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FluidModificationItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
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
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

public abstract class InteractBlockCallback {

	private static final int RADIUS = 2;

	public static ActionResult interactBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
		if (world.isClient) return ActionResult.PASS;
		else if (hand != Hand.MAIN_HAND) return ActionResult.PASS;

		final BlockPos blockPos = hitResult.getBlockPos();
		final BlockState blockState = world.getBlockState(blockPos);

		if (InteractBlockCallback.canSeatAt(player, world, hitResult, blockState, blockPos)) {
			final double x = blockPos.getX() + .5d;
			final double y = blockPos.getY() + (blockState.isFullCube(world, blockPos) ? 1d : .5d);
			final double z = blockPos.getZ() + .5d;
			final Vec3d pos = blockState.isIn(BlockTags.STAIRS) ? switch (blockState.get(StairsBlock.FACING)) {
				case NORTH -> new Vec3d(x, y, z + .1f);
				case SOUTH -> new Vec3d(x, y, z - .1f);
				case WEST -> new Vec3d(x + .1f, y, z);
				case EAST -> new Vec3d(x - .1f, y, z);
				default -> throw new IllegalStateException(blockState.get(StairsBlock.FACING).asString());
			} : new Vec3d(x, y, z);
			if (!SeatEntity.hasSeatAt(world, pos)) FSitMod.spawnSeat(player, world, pos);

			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}

	private static boolean canSeatAt(@NotNull PlayerEntity player, World world, BlockHitResult hitResult, @NotNull BlockState blockState, BlockPos blockPos) {
		final Block block = blockState.getBlock();
		final Item mainItem = player.getMainHandStack().getItem();
		final Item offItem = player.getOffHandStack().getItem();

		if (mainItem instanceof BlockItem || offItem instanceof BlockItem) return false;
		else if (mainItem instanceof FluidModificationItem || offItem instanceof FluidModificationItem) return false;

		if (Math.abs(player.getBlockPos().getX() - blockPos.getX()) > RADIUS) return false;
		else if (Math.abs(player.getBlockPos().getZ() - blockPos.getZ()) > RADIUS) return false;
		else if (hitResult.getSide() != Direction.UP || player.getY() - blockPos.getY() > RADIUS) return false;
		else if (!player.isOnGround() || player.shouldCancelInteraction()) return false;

		final BlockState blockAbove = world.getBlockState(blockPos.up());
		if (!blockAbove.isAir()) return false;

		final Set<Identifier> blockTags = blockState.streamTags().map(TagKey::id).collect(Collectors.toUnmodifiableSet());
		for (Identifier blockTag : blockTags)
			if (FSitConfig.sittableTags.getIds().contains(blockTag)) return InteractBlockCallback.isBottom(block, blockState);

		final Identifier blockId = Registries.BLOCK.getId(block);
		if (FSitConfig.sittableBlocks.getIds().contains(blockId)) return InteractBlockCallback.isBottom(block, blockState);

		return false;
	}

	private static boolean isBottom(Block block, BlockState blockState) {
		if (block instanceof PillarBlock) {
			return blockState.get(Properties.AXIS) != Direction.Axis.Y;
		} else if (block instanceof StairsBlock) {
			return blockState.get(Properties.BLOCK_HALF) == BlockHalf.BOTTOM;
		} else if (block instanceof SlabBlock) {
			return blockState.get(Properties.SLAB_TYPE) == SlabType.BOTTOM;
		} else return true;
	}
}
