package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.entity.SeatEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

	@Inject(method = "onDisconnect", at = @At(value = "HEAD"))
	public void onDisconnect(CallbackInfo ci) {
		final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
		final Entity vehicle = player.getVehicle();
		if (vehicle instanceof SeatEntity) player.stopRiding();
	}
}
