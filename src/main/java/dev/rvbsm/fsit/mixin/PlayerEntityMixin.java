package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.entity.CrawlEntity;
import dev.rvbsm.fsit.entity.PlayerConfigAccessor;
import dev.rvbsm.fsit.entity.PlayerPose;
import dev.rvbsm.fsit.entity.PlayerPoseAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerPoseAccessor {

	@Unique
	private static final BlockState AIR = Blocks.AIR.getDefaultState();
	@Unique
	private static final BlockState BARRIER = Blocks.BARRIER.getDefaultState();
	@Unique
	private BlockPos blockAbove = null;
	@Unique
	private CrawlEntity entityAbove = null;
	@Unique
	private PlayerPose playerPose;

	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "updatePose", at = @At("HEAD"))
	public void updatePose(CallbackInfo ci) {
		if ((PlayerEntity) (Object) this instanceof ServerPlayerEntity player) {
			final PlayerPoseAccessor poseAccessor = (PlayerPoseAccessor) player;
			final PlayerConfigAccessor configAccessor = (PlayerConfigAccessor) player;
			final World world = player.getWorld();
			BlockPos blockPos = player.getBlockPos().up();
			if (player.getMovementSpeed() > 4.5f) blockPos = blockPos.offset(player.getMovementDirection());
			final Vec3d entityPos = blockPos.toCenterPos();

			final boolean placeBarrier = world.getBlockState(blockPos).isAir();
			final boolean placeShulker = !placeBarrier && !world.getBlockState(blockPos).isSideSolidFullSquare(world, blockPos, Direction.DOWN);

			if (this.blockAbove != null) {
				player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.blockAbove, AIR));
				this.blockAbove = null;
			}
			if (this.entityAbove != null && !placeShulker) {
				player.networkHandler.sendPacket(new EntitiesDestroyS2CPacket(this.entityAbove.getId()));
				this.entityAbove = null;
			}

			if (poseAccessor.isInPose(PlayerPose.CRAWL)) {
				player.setSwimming(true);
				if (configAccessor.fsit$isModded()) return;

				if (placeShulker) {
					if (this.entityAbove == null) {
						this.entityAbove = new CrawlEntity(world, entityPos);
						player.networkHandler.sendPacket(this.entityAbove.createSpawnPacket());
						player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(this.entityAbove.getId(), this.entityAbove.getDataTracker().getChangedEntries()));
					} else if (!this.entityAbove.getPos().equals(entityPos)) {
						this.entityAbove.setPosition(entityPos);
						player.networkHandler.sendPacket(new EntityPositionS2CPacket(this.entityAbove));
					}
				} else if (placeBarrier) {
					this.blockAbove = blockPos;
					player.networkHandler.sendPacket(new BlockUpdateS2CPacket(blockPos, BARRIER));
				}
			}
		}
	}


	@Override
	public PlayerPose fsit$getPose() {
		return this.playerPose;
	}

	@Override
	public void fsit$setPose(PlayerPose pose) {
		this.playerPose = pose;
	}
}
