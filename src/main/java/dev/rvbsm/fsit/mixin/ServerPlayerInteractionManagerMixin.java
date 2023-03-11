package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.FSitMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

	private final FSitMod FSit = FSitMod.getInstance();

	@Inject(at = @At(value = "HEAD"), method = "interactBlock", locals = LocalCapture.CAPTURE_FAILHARD)
	public void interactBlock(ServerPlayerEntity player, @NotNull World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
		if (world.isClient) return;
		final BlockPos blockPos = hitResult.getBlockPos();
		final BlockState blockState = world.getBlockState(blockPos);
		final Block block = blockState.getBlock();

		if (block instanceof SlabBlock || block instanceof StairsBlock) {
			if (player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof BlockItem) return;
			else if (player.getStackInHand(Hand.OFF_HAND).getItem() instanceof BlockItem) return;

			final BlockState blockAbove = world.getBlockState(blockPos.up());
			if (!blockAbove.isAir()) return;
			if (Math.abs(player.getBlockX() - blockPos.getX()) > 1) return;
			else if (Math.abs(player.getBlockZ() - blockPos.getZ()) > 1) return;
			else if (player.getY() < blockPos.getY() - .5) return;
			else if (player.getY() - blockPos.getY() > 2) return;
			else if (!player.isOnGround() && !player.hasVehicle() || player.isSneaking()) return;

			if (block instanceof SlabBlock) {
				if (blockState.get(Properties.SLAB_TYPE) == SlabType.BOTTOM)
					FSit.spawnSeat(player, world, blockPos.getX() + .5f, blockPos.getY() + .5f, blockPos.getZ() + .5f);
			} else {
				if (blockState.get(Properties.BLOCK_HALF) == BlockHalf.BOTTOM)
					FSit.spawnSeat(player, world, blockPos.getX() + .5f, blockPos.getY() + .5f, blockPos.getZ() + .5f);
			}
		}
	}
}
