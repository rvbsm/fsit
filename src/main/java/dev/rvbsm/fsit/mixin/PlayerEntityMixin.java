package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.entity.CrawlEntity;
import dev.rvbsm.fsit.entity.PlayerPose;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

	private static final BlockState AIR = Blocks.AIR.getDefaultState();
	private static final BlockState BARRIER = Blocks.BARRIER.getDefaultState();
	private BlockPos blockAbove = null;
	private CrawlEntity entityAbove = null;

	@Inject(method = "updatePose", at = @At("HEAD"))
	public void updatePose(CallbackInfo ci) {
		if ((PlayerEntity) (Object) this instanceof ServerPlayerEntity player) {
			if (this.blockAbove != null) {
				player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.blockAbove, AIR));
				this.blockAbove = null;
			}
			if (FSitMod.isInPose(player.getUuid(), PlayerPose.CRAWL)) {
				player.setSwimming(true);
				if (FSitMod.isModded(player.getUuid())) return;

				final Vec3d entityPos = player.getPos().offset(Direction.UP, 1.0d);
				final BlockPos blockPos = new BlockPos((int) entityPos.x, (int) entityPos.y, (int) entityPos.z);
				final World world = player.getWorld();
				if (world.getBlockState(blockPos).isAir()) {
					this.blockAbove = blockPos;
					player.networkHandler.sendPacket(new BlockUpdateS2CPacket(blockPos, BARRIER));
				} else if (!world.getBlockState(blockPos).isSideSolidFullSquare(world, blockPos, Direction.DOWN)) {
					if (this.entityAbove == null) {
						this.entityAbove = new CrawlEntity(world, entityPos);
						player.networkHandler.sendPacket(new EntitySpawnS2CPacket(this.entityAbove));
						player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(this.entityAbove.getId(), this.entityAbove.getDataTracker().getChangedEntries()));
					}
					if (!this.entityAbove.getPos().equals(entityPos)) {
						this.entityAbove.setPosition(entityPos);
						player.networkHandler.sendPacket(new EntityPositionS2CPacket(this.entityAbove));
					}
				} else if (this.entityAbove != null) {
					player.networkHandler.sendPacket(new EntitiesDestroyS2CPacket(this.entityAbove.getId()));
					this.entityAbove = null;
				}
			} else if (this.entityAbove != null) {
				player.networkHandler.sendPacket(new EntitiesDestroyS2CPacket(this.entityAbove.getId()));
				this.entityAbove = null;
			}
		}
	}
}
