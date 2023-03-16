package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.FSitMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FluidModificationItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

	private static final FSitMod FSit = FSitMod.getInstance();
	private static final int RADIUS = 2;

	@Inject(at = @At(value = "HEAD"), method = "interactBlock", locals = LocalCapture.CAPTURE_FAILHARD)
	public void interactBlock(ServerPlayerEntity player, @NotNull World world, ItemStack stack, Hand hand, @NotNull BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
		if (hand != Hand.MAIN_HAND) return;
		final BlockPos blockPos = hitResult.getBlockPos();
		final BlockState blockState = world.getBlockState(blockPos);
		final Block block = blockState.getBlock();

		if (this.canSeatAt(player, world, block, blockState, blockPos)) {
			final double x = blockPos.getX() + .5f;
			final double y = blockPos.getY() + (blockState.isSolidBlock(world, blockPos) ? 1d : .5d);
			final double z = blockPos.getZ() + .5f;
			if (!FSit.hasSeatAt(x, y, z)) FSit.spawnSeat(player, world, x, y, z);
		}
	}

	private boolean canSeatAt(@NotNull ServerPlayerEntity player, World world, Block block, BlockState blockState, BlockPos blockPos) {
		final Item mainItem = player.getMainHandStack().getItem();
		final Item offItem = player.getOffHandStack().getItem();

		if (mainItem instanceof BlockItem || offItem instanceof BlockItem) return false;
		else if (mainItem instanceof FluidModificationItem || offItem instanceof FluidModificationItem) return false;

		if (Math.abs(player.getBlockPos().getX() - blockPos.getX()) > RADIUS) return false;
		else if (Math.abs(player.getBlockPos().getZ() - blockPos.getZ()) > RADIUS) return false;
		else if (Math.round(player.getY()) < blockPos.getY() || player.getY() - blockPos.getY() > RADIUS) return false;
		else if (!player.isOnGround() && !player.hasVehicle() || !player.isSneaking()) return false;

		final BlockState blockAbove = world.getBlockState(blockPos.up());
		if (!blockAbove.isAir()) return false;

		// ! calls every time players click!
		for (TagKey<Block> configTag : FSit.getConfig().sittableTags.getTagKeySet())
			if (blockState.isIn(configTag)) if (block instanceof PillarBlock) {
				return blockState.get(Properties.AXIS) != Direction.Axis.Y;
			} else return true;

		for (Block configBlock : FSit.getConfig().sittableBlocks.getBlocks())
			if (block.equals(configBlock)) if (block instanceof PillarBlock) {
				return blockState.get(Properties.AXIS) != Direction.Axis.Y;
			} else return true;

		return false;
	}
}
