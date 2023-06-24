package dev.rvbsm.fsit.mixin.client;

import dev.rvbsm.fsit.FSitClientMod;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.entity.PlayerPose;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

	@Inject(method = "tickMovement", at = @At("TAIL"))
	public void isSneaking(CallbackInfo ci) {
		final ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
		if (FSitClientMod.isInPose(PlayerPose.CRAWL)) player.setSwimming(true);
	}
}
