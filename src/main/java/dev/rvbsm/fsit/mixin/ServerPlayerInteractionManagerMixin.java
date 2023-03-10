package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.FSitMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

	private final FSitMod FSit = FSitMod.getInstance();

	@Inject(at = @At(value = "HEAD"), method = "interactBlock")
	public void interactBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
		final BlockPos blockPos = hitResult.getBlockPos();
		if (player.getY() < blockPos.getY()) return;
		if (!player.isOnGround()) return;
		if (player.isSneaking()) return;
		if (player.getStackInHand(Hand.MAIN_HAND) != ItemStack.EMPTY) return;

		final BlockState blockState = world.getBlockState(blockPos);
		final Block block = blockState.getBlock();

		if (player.hasVehicle()) player.dismountVehicle();
		if (block instanceof SlabBlock) {
			if (blockState.get(Properties.SLAB_TYPE) == SlabType.BOTTOM)
				FSit.spawnSeat(player, world, blockPos.getX() + .5f, blockPos.getY() + .5f, blockPos.getZ() + .5f);
		} else if (block instanceof StairsBlock) {
			if (blockState.get(Properties.BLOCK_HALF) == BlockHalf.BOTTOM)
				FSit.spawnSeat(player, world, blockPos.getX() + .5f, blockPos.getY() + .5f, blockPos.getZ() + .5f);
		}
	}
}
