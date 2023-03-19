package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.entity.SeatEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

	@Inject(method = "onDisconnect", at = @At(value = "HEAD"))
	public void onDisconnect(CallbackInfo ci) {
		final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
		final Entity vehicle = player.getVehicle();
		if (vehicle instanceof SeatEntity) player.stopRiding();
	}

	@Inject(method = "changeGameMode", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;stopRiding()V"))
	public void changeGameMode(GameMode gameMode, CallbackInfoReturnable<Boolean> cir) {
		final PlayerEntity player = (ServerPlayerEntity) (Object) this;
		if (player.getFirstPassenger() instanceof PlayerEntity) player.getFirstPassenger().stopRiding();
	}
}
