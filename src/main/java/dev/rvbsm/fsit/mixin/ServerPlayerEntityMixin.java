package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.entity.ConfigHandler;
import dev.rvbsm.fsit.entity.PlayerPose;
import dev.rvbsm.fsit.entity.RestrictHandler;
import dev.rvbsm.fsit.entity.SeatEntity;
import dev.rvbsm.fsit.network.packet.PoseSyncS2CPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntityMixin implements ConfigHandler {

	@Unique
	private ConfigData config = FSitMod.getConfig();

	protected ServerPlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Shadow
	public abstract void sendMessage(Text message, boolean overlay);

	@Override
	public ConfigData fsit$getConfig() {
		return this.config;
	}

	@Override
	public void fsit$setConfig(ConfigData config) {
		this.config = config;
		if (this.hasPassengers() && !config.getRiding().isEnabled()) this.removeAllPassengers();
	}

	@Override
	public boolean fsit$hasConfig() {
		return this.config == FSitMod.getConfig();
	}

	@Override
	public boolean fsit$canStartRiding(PlayerEntity player) {
		final ConfigHandler configHandler = (ConfigHandler) player;
		final RestrictHandler restrictHandler = (RestrictHandler) player;
		final ConfigData.RidingTable rideConfig = configHandler.fsit$getConfig().getRiding();

		return !this.fsit$isRestricted(player.getUuid()) && !restrictHandler.fsit$isRestricted(this.getUuid()) && this.config.getRiding()
						.isEnabled() && rideConfig.isEnabled() && player.distanceTo(this) <= rideConfig.getRadius();
	}

	@Override
	public void fsit$restrictPlayer(UUID playerUUID) {
		super.fsit$restrictPlayer(playerUUID);
		if (this.hasPassenger((passenger -> passenger.getUuid() == playerUUID))) this.removeAllPassengers();
	}

	@Override
	public void fsit$setPose(PlayerPose pose) {
		super.fsit$setPose(pose);

		if (this.fsit$hasConfig())
			ServerPlayNetworking.send((ServerPlayerEntity) (Object) this, new PoseSyncS2CPacket(pose));
		else if (this.isPosing()) this.sendMessage(Text.of("Press Sneak key to get up"), true);
	}

	@Unique
	private boolean preventsPosing() {
		return this.isSpectator() || !this.isOnGround() || this.hasVehicle();
	}

	@Override
	public void fsit$setSneaked() {
		if (this.isPosing() || this.preventsPosing()) return;

		this.fsit$setPose(PlayerPose.SNEAK);
		final Executor delayedExecutor = CompletableFuture.delayedExecutor(this.config.getSneak()
						.getDelay(), TimeUnit.MILLISECONDS);
		CompletableFuture.runAsync(() -> {
			if (!this.isPosing()) this.resetPose();
		}, delayedExecutor);
	}

	@Override
	public void fsit$setSitting() {
		this.fsit$setSitting(this.getPos());
	}

	@Override
	public void fsit$setSitting(Vec3d pos) {
		if (this.preventsPosing()) return;

		final BlockPos blockPos = this.getBlockPos();
		final BlockState blockBelowState = this.getWorld()
						.getBlockState(this.getPos().y % 1 == 0 ? blockPos.down() : blockPos);
		if (blockBelowState.isAir()) return;
		this.fsit$setPose(PlayerPose.SIT);

		final World world = this.getWorld();
		final SeatEntity seat = new SeatEntity(world, pos);
		world.spawnEntity(seat);
		this.startRiding(seat, true);
	}

	@Override
	public void fsit$setCrawling() {
		if (this.preventsPosing()) return;

		this.fsit$setPose(PlayerPose.CRAWL);
	}

	@Inject(method = "stopRiding", at = @At("TAIL"))
	private void stopRiding(CallbackInfo ci) {
		if (this.isInPose(PlayerPose.SIT)) this.resetPose();
	}
}
