package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.config.FSitConfig;
import dev.rvbsm.fsit.entity.SeatEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

	@Inject(method = "shouldDismount", at = @At(value = "HEAD"), cancellable = true)
	protected void shouldDismount(CallbackInfoReturnable<Boolean> cir) {
		final PlayerEntity player = (PlayerEntity) (Object) this;
		if (player.world.isClient) return;

		// ! bruh
		if (player.getVehicle() instanceof final SeatEntity seatEntity) if (seatEntity.age <= 10) cir.setReturnValue(false);
	}

	@Inject(method = "interact", at = @At(value = "HEAD"))
	public void interact(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		if (!FSitConfig.sitOnPlayers.getValue()) return;

		final PlayerEntity player = (PlayerEntity) (Object) this;
		if (player.isSpectator() || player.hasVehicle()) return;

		if (entity instanceof PlayerEntity otherPlayer) {
			if (otherPlayer.isSpectator() || otherPlayer.hasPassengers()) return;
			player.startRiding(otherPlayer, true);
		}
	}
}
