package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.FSitMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

	private static final BlockState AIR = Blocks.AIR.getDefaultState();
	private static final BlockState BARRIER = Blocks.BARRIER.getDefaultState();
	private BlockPos blockAbove = null;

	@Redirect(method = "updatePose", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setPose(Lnet/minecraft/entity/EntityPose;)V"))
	public void updatePose$setPose(PlayerEntity instance, EntityPose entityPose) {
		if (instance instanceof ServerPlayerEntity player) {
			if (this.blockAbove != null) {
				player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.blockAbove, AIR));
				this.blockAbove = null;
			}
			if (FSitMod.isCrawling(player.getUuid())) {
				entityPose = EntityPose.SWIMMING;
				final BlockPos blockAbove = player.isOnGround() ? player.getBlockPos().up() : player.getBlockPos().up(2);
				if (player.getWorld().getBlockState(blockAbove).isAir() && !FSitMod.isModded(player.getUuid())) {
					this.blockAbove = blockAbove;
					player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.blockAbove, BARRIER));
				}
			}
		}

		instance.setPose(entityPose);
	}
}
