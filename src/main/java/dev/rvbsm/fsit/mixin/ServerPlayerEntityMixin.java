package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.entity.PlayerConfigAccessor;
import dev.rvbsm.fsit.entity.PlayerPose;
import dev.rvbsm.fsit.entity.SeatEntity;
import dev.rvbsm.fsit.packet.PoseSyncS2CPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntityMixin implements PlayerConfigAccessor {

	@Unique
	private ConfigData config = FSitMod.getConfig();
	@Unique
	private boolean isModded = false;

	protected ServerPlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Shadow
	public abstract void sendMessage(Text message, boolean overlay);

	@Inject(method = "stopRiding", at = @At("TAIL"))
	public void stopRiding(CallbackInfo ci) {
		if (this.isInPose(PlayerPose.SIT)) this.resetPose();
	}


	@Override
	public ConfigData fsit$getConfig() {
		return this.config;
	}

	@Override
	public void fsit$setConfig(ConfigData config) {
		this.config = config;
		this.isModded = true;
	}

	@Override
	public void fsit$setPose(PlayerPose pose) {
		super.fsit$setPose(pose);

		if (this.isModded) ServerPlayNetworking.send((ServerPlayerEntity) (Object) this, new PoseSyncS2CPacket(pose));
		else if (this.isPosing()) this.sendMessage(Text.of("Press Sneak key to get up"), true);
	}

	@Override
	public boolean fsit$isModded() {
		return this.isModded;
	}

	@Override
	public void fsit$setSneaked() {
		if (this.isPosing() || this.preventsPosing()) return;

		this.fsit$setPose(PlayerPose.SNEAK);
		final Executor delayedExecutor = CompletableFuture.delayedExecutor(this.config.sneakDelay, TimeUnit.MILLISECONDS);
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
		final BlockState blockBelowState = this.getWorld().getBlockState(this.getPos().y % 1 == 0 ? blockPos.down() : blockPos);
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

	@Unique
	private boolean preventsPosing() {
		return this.isSpectator() || !this.isOnGround() || this.hasVehicle();
	}
}
