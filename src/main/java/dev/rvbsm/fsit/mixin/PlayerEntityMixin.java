package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.config.FSitConfig;
import dev.rvbsm.fsit.entity.SeatEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
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
		if (player.world.isClient || player.isSpectator() || player.hasVehicle()) return;

		if (entity instanceof PlayerEntity) if (!entity.isSpectator() && !entity.hasPassengers()) {
			final ServerPlayerEntity passenger = (ServerPlayerEntity) entity;

			passenger.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(player));
			player.startRiding(passenger, true);
			passenger.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(passenger));
		}
	}
}
