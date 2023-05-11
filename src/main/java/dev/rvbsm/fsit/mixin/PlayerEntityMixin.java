package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.entity.SeatEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

	@Environment(EnvType.SERVER)
	@Inject(method = "shouldDismount", at = @At(value = "HEAD"), cancellable = true)
	protected void shouldDismount(CallbackInfoReturnable<Boolean> cir) {
		final PlayerEntity player = (PlayerEntity) (Object) this;

		// ! bruh
		if (player.getVehicle() instanceof final SeatEntity seatEntity) if (seatEntity.age <= 10) cir.setReturnValue(false);
	}
}
