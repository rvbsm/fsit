package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.entity.SeatEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

	@Shadow public abstract void stopRiding();

	@Inject(at = @At(value = "HEAD"), method = "onDisconnect")
	public void onDisconnect(CallbackInfo ci) {
		final Entity vehicle = ((PlayerEntity) (Object) this).getVehicle();
		if (vehicle instanceof SeatEntity) this.stopRiding();
	}
}
