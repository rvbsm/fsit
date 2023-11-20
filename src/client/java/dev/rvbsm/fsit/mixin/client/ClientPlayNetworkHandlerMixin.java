package dev.rvbsm.fsit.mixin.client;

import dev.rvbsm.fsit.FSitMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin extends ClientCommonNetworkHandler {

	protected ClientPlayNetworkHandlerMixin(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
		super(client, connection, connectionState);
	}

	@Inject(method = "onEntityPassengersSet", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;startRiding(Lnet/minecraft/entity/Entity;Z)Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void onEntityPassengersSet$startRiding$sendOnRideMessage(EntityPassengersSetS2CPacket packet, CallbackInfo ci, Entity passenger, boolean bl, int[] var4, int var5, int var6, int i, Entity vehicle) {
		if (passenger == this.client.player && vehicle.isPlayer()) {
			final Text text = FSitMod.getTranslation("message", "onride", this.client.options.sneakKey.getBoundKeyLocalizedText());
			this.client.inGameHud.setOverlayMessage(text, false);
			this.client.getNarratorManager().narrate(text);
		}
	}
}
