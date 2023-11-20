package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.entity.ConfigHandler;
import dev.rvbsm.fsit.entity.CrawlEntity;
import dev.rvbsm.fsit.entity.PlayerPose;
import dev.rvbsm.fsit.entity.PoseHandler;
import dev.rvbsm.fsit.entity.RestrictHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PoseHandler, RestrictHandler {

	@Unique
	private static final String RESTRICTION_LIST_KEY = "restrictionList";
	@Unique
	private static final BlockState BARRIER = Blocks.BARRIER.getDefaultState();
	@Unique
	protected final Set<UUID> restrictionList = new HashSet<>();
	@Unique
	private BlockPos supportBlockPos = null;
	@Unique
	private CrawlEntity supportEntity = null;
	@Unique
	private PlayerPose playerPose = PlayerPose.NONE;

	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Shadow
	public abstract float getMovementSpeed();

	@Shadow
	public abstract PlayerAbilities getAbilities();

	@Override
	public PlayerPose fsit$getPose() {
		return this.playerPose;
	}

	@Override
	public void fsit$setPose(PlayerPose pose) {
		this.playerPose = pose;
	}

	@Override
	public Set<UUID> fsit$getRestrictionList() {
		return this.restrictionList;
	}

	@Override
	public boolean fsit$isRestricted(UUID playerUUID) {
		return this.restrictionList.contains(playerUUID);
	}

	@Override
	public void fsit$allowPlayer(UUID playerUUID) {
		this.restrictionList.remove(playerUUID);
	}

	@Override
	public void fsit$restrictPlayer(UUID playerUUID) {
		this.restrictionList.add(playerUUID);
	}

	@Unique
	private BlockPos getSupportBlockPos() {
		final BlockPos blockPos = this.getBlockPos().up();
		if (this.getMovementSpeed() > 4.5f) return blockPos.offset(this.getMovementDirection());

		return blockPos;
	}

	@Unique
	private void clearSupport(boolean placeEntity) {
		if ((PlayerEntity) (Object) this instanceof ServerPlayerEntity player) {
			if (this.supportBlockPos != null) {
				final BlockState originalBlockState = this.getWorld().getBlockState(this.supportBlockPos);
				player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.supportBlockPos, originalBlockState));
				this.supportBlockPos = null;
			}
			if (this.supportEntity != null && !placeEntity) {
				player.networkHandler.sendPacket(new EntitiesDestroyS2CPacket(this.supportEntity.getId()));
				this.supportEntity = null;
			}
		}
	}

	@Unique
	private void setSupportEntity(Vec3d entityPos) {
		if ((PlayerEntity) (Object) this instanceof ServerPlayerEntity player) {
			if (this.supportEntity == null) {
				this.supportEntity = new CrawlEntity(this.getWorld(), entityPos);
				player.networkHandler.sendPacket(this.supportEntity.createSpawnPacket());
				player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(this.supportEntity.getId(), this.supportEntity.getDataTracker()
								.getChangedEntries()));
			} else if (!this.supportEntity.getPos().equals(entityPos)) {
				this.supportEntity.setPosition(entityPos);
				player.networkHandler.sendPacket(new EntityPositionS2CPacket(this.supportEntity));
			}
		}
	}

	@Unique
	private void setSupportBlock(BlockPos blockPos, BlockState state) {
		if (!this.getWorld().isClient) {
			this.supportBlockPos = blockPos;
			((ServerPlayerEntity) (Object) this).networkHandler.sendPacket(new BlockUpdateS2CPacket(blockPos, state));
		}
	}

	@Inject(method = "updatePose", at = @At("HEAD"))
	private void updatePose$handlePosing(CallbackInfo ci) {
		final PoseHandler poseHandler = this;
		if (this.getAbilities().flying) poseHandler.resetPose();
		else if (poseHandler.isInPose(PlayerPose.CRAWL)) this.setSwimming(true);

		if (!this.getWorld().isClient && !((ConfigHandler) this).fsit$hasConfig()) {
			final World world = this.getWorld();
			final BlockPos supportBlockPos = this.getSupportBlockPos();
			final boolean placeBlock = world.getBlockState(supportBlockPos).isAir();

			if (poseHandler.isInPose(PlayerPose.CRAWL)) {
				final Vec3d supportEntityPos = Vec3d.of(supportBlockPos);
				final boolean placeEntity = !placeBlock && !world.getBlockState(supportBlockPos)
								.isSideSolidFullSquare(world, supportBlockPos, Direction.DOWN);

				this.clearSupport(placeEntity);
				if (placeEntity) this.setSupportEntity(supportEntityPos);
				else if (placeBlock) this.setSupportBlock(supportBlockPos, BARRIER);
			} else this.clearSupport(false);
		}
	}

	@Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
	private void readCustomDataFromNbt$readRestrictionList(NbtCompound nbt, CallbackInfo ci) {
		final long[] restrictionLongArray = nbt.getLongArray(RESTRICTION_LIST_KEY);
		for (int i = 0; i < restrictionLongArray.length; )
			this.fsit$restrictPlayer(new UUID(restrictionLongArray[i++], restrictionLongArray[i++]));
	}

	@Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
	private void writeCustomDataToNbt$writeRestrictionList(NbtCompound nbt, CallbackInfo ci) {
		final long[] restrictionLongArray = new long[this.restrictionList.size() * 2];
		int i = 0;
		for (UUID uuid : this.restrictionList) {
			restrictionLongArray[i++] = uuid.getMostSignificantBits();
			restrictionLongArray[i++] = uuid.getLeastSignificantBits();
		}
		nbt.putLongArray(RESTRICTION_LIST_KEY, restrictionLongArray);
	}
}
