package dev.rvbsm.fsit.event;

import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.entity.PlayerConfigAccessor;
import dev.rvbsm.fsit.entity.PlayerPoseAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FluidModificationItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public final class InteractSBlockCallback {

	private InteractSBlockCallback() {}

	public static ActionResult interact(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
		if (world.isClient) return ActionResult.PASS;
		final PlayerPoseAccessor poseAccessor = (PlayerPoseAccessor) player;
		final PlayerConfigAccessor configAccessor = (PlayerConfigAccessor) player;
		final ConfigData config = configAccessor.fsit$getConfig();

		if (configAccessor.fsit$isModded()) return ActionResult.PASS;
		else if (!config.sittable) return ActionResult.PASS;

		final Item handItem = player.getStackInHand(hand).getItem();
		if (handItem instanceof BlockItem) return ActionResult.PASS;
		else if (handItem instanceof FluidModificationItem) return ActionResult.PASS;
		else if (player.shouldCancelInteraction()) return ActionResult.PASS;

		if (player.getPos().distanceTo(hitResult.getPos()) <= config.sittableRadius && InteractSBlockCallback.isSittable(world, hitResult, config.sittableTags, config.sittableBlocks)) {
			poseAccessor.fsit$setSitting(hitResult.getPos());
			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}

	public static boolean isSittable(World world, BlockHitResult hitResult, List<Identifier> sittableTags, List<Identifier> sittableBlocks) {
		if (hitResult.getSide() != Direction.UP) return false;

		final BlockPos blockPos = hitResult.getBlockPos();
		if (!world.isAir(blockPos.up())) return false;

		final BlockState blockState = world.getBlockState(blockPos);
		final Block block = blockState.getBlock();

		final Stream<Identifier> blockTags = blockState.streamTags().map(TagKey::id);
		final Identifier blockIdentifier = Registries.BLOCK.getId(block);
		if (blockTags.anyMatch(sittableTags::contains) || sittableBlocks.contains(blockIdentifier)) {
			final Collection<Property<?>> blockProperties = blockState.getProperties();
			if (blockProperties.contains(Properties.AXIS))
				return blockState.get(Properties.AXIS) != Direction.Axis.Y;
			else if (blockProperties.contains(Properties.BLOCK_HALF))
				return blockState.get(Properties.BLOCK_HALF) == BlockHalf.BOTTOM;
			else if (blockProperties.contains(Properties.SLAB_TYPE))
				return blockState.get(Properties.SLAB_TYPE) == SlabType.BOTTOM;
			else return true;
		}

		return false;
	}
}
