package dev.rvbsm.fsit.mixin.client;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.entity.PlayerPoseAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(method = "onEntityPassengersSet", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;startRiding(Lnet/minecraft/entity/Entity;Z)Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
	public void onEntityPassengersSet$startRiding(EntityPassengersSetS2CPacket packet, CallbackInfo ci, Entity entity, boolean bl, int[] var4, int var5, int var6, int i, Entity entity2) {
		if (entity == this.client.player) {
			final Text text = FSitMod.getTranslation("message", "onride", this.client.options.sneakKey.getBoundKeyLocalizedText());
			this.client.inGameHud.setOverlayMessage(text, false);
			this.client.getNarratorManager().narrate(text);
		}
	}

	@ModifyConstant(method = "onEntityPassengersSet", constant = @Constant(stringValue = "mount.onboard"))
	private String onEntityPassengersSet$key(String constant) {
		final ClientPlayerEntity player;
		return (player = this.client.player) != null && ((PlayerPoseAccessor) player).isPosing()
						? FSitMod.getTranslationKey("message", "onpose")
						: constant;
	}
}
