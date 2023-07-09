package dev.rvbsm.fsit.mixin.client;

import dev.rvbsm.fsit.FSitMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

//	@Shadow
//	@Final
//	private MinecraftClient client;
//
//	@Redirect(method = "onEntityPassengersSet", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;setOverlayMessage(Lnet/minecraft/text/Text;Z)V"))
//	public void onEntityPassengersSet(InGameHud inGameHud, Text message, boolean tinted) {
//		if (this.client.player.getFirstPassenger() instanceof PlayerEntity)
//			message = FSitMod.getTranslation("message", "onpose", this.client.options.sneakKey.getBoundKeyLocalizedText());
//
//		inGameHud.setOverlayMessage(message, tinted);
//	}
}
