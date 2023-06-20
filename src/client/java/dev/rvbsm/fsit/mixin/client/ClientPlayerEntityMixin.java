package dev.rvbsm.fsit.mixin.client;

import dev.rvbsm.fsit.FSitClientMod;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.packet.SpawnSeatC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

	@Shadow
	public Input input;
	@Shadow
	@Final
	protected MinecraftClient client;
	private boolean prevSneaking;

	@Shadow
	public abstract boolean isMainPlayer();

	@Inject(method = "tickMovement", at = @At("TAIL"))
	public void isSneaking(CallbackInfo ci) {
		final ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
		if (!this.isMainPlayer()) return;
		if (FSitClientMod.isCrawling()) player.setSwimming(true);
		if (!player.isOnGround() || player.hasVehicle() || player.isSpectator()) return;

		if (this.prevSneaking && !this.input.sneaking) {
			if (FSitClientMod.isCrawling()) FSitClientMod.setCrawling(false);
			else if (player.getPitch() >= FSitMod.config.minAngle) {
				if (FSitClientMod.isSneaked()) {
					if (player.isCrawling()) {
						FSitClientMod.setCrawling(true);
						player.sendMessage(FSitMod.getTranslation("message", "oncrawl", this.client.options.sneakKey.getBoundKeyLocalizedText()), true);
					} else ClientPlayNetworking.send(new SpawnSeatC2SPacket(player.getPos(), player.getPos()));
				} else if (FSitMod.config.sneakSit) FSitClientMod.setSneaked();
			}
		}

		this.prevSneaking = this.input.sneaking;
	}
}
