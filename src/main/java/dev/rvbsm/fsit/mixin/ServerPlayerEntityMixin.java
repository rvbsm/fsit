package dev.rvbsm.fsit.mixin;

import com.mojang.authlib.GameProfile;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.entity.PlayerConfigAccessor;
import dev.rvbsm.fsit.entity.PlayerPose;
import dev.rvbsm.fsit.entity.PlayerPoseAccessor;
import dev.rvbsm.fsit.entity.SeatEntity;
import dev.rvbsm.fsit.packet.PoseSyncS2CPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements PlayerPoseAccessor, PlayerConfigAccessor {

	private PlayerPose playerPose;
	private ConfigData config;
	private boolean isModded = false;

	public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
		this.config = FSitMod.getConfig();
	}

	@Inject(method = "stopRiding", at = @At("TAIL"))
	public void stopRiding(CallbackInfo ci) {
		if (this.isInPlayerPose(PlayerPose.SIT)) this.resetPlayerPose();
	}

	@Override
	public ConfigData getConfig() {
		return this.config;
	}

	@Override
	public void setConfig(ConfigData config) {
		this.config = config;
		this.isModded = true;
	}

	@Override
	public PlayerPose getPlayerPose() {
		return this.playerPose;
	}

	@Override
	public void setPlayerPose(PlayerPose pose) {
		this.playerPose = pose;

		if (this.isModded) ServerPlayNetworking.send((ServerPlayerEntity) (Object) this, new PoseSyncS2CPacket(pose));
		else if (this.isPlayerPosing()) this.sendMessage(Text.of("Press Sneak key to get up"), true);
	}

	@Override
	public boolean isModded() {
		return this.isModded;
	}

	@Override
	public void setPlayerSneaked() {
		if (!this.canStartPosing()) return;
		else if (this.isPlayerPosing()) return;

		this.setPlayerPose(PlayerPose.SNEAK);
		final Executor delayedExecutor = CompletableFuture.delayedExecutor(this.config.sneakDelay, TimeUnit.MILLISECONDS);
		CompletableFuture.runAsync(() -> {
			if (!this.isPlayerPosing()) this.resetPlayerPose();
		}, delayedExecutor);
	}

	@Override
	public void setPlayerSitting() {
		this.setPlayerSitting(this.getPos());
	}

	@Override
	public void setPlayerSitting(Vec3d pos) {
		if (!this.canStartPosing()) return;

		final BlockPos blockPos = this.getBlockPos();
		final BlockState blockBelowState = this.getWorld().getBlockState(this.getPos().y % 1 == 0 ? blockPos.down() : blockPos);
		if (blockBelowState.isAir()) return;
		this.setPlayerPose(PlayerPose.SIT);

		final World world = this.getWorld();
		final SeatEntity seat = new SeatEntity(world, pos);
		world.spawnEntity(seat);
		this.startRiding(seat, true);
	}

	@Override
	public void setPlayerCrawling() {
		if (!this.canStartPosing()) return;

		this.setPlayerPose(PlayerPose.CRAWL);
	}

	private boolean canStartPosing() {
		return !this.isSpectator() && this.isOnGround() && !this.hasVehicle();
	}
}
