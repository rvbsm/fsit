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

	@Shadow
	public Input input;
	private boolean prevSneaking;

	@Shadow
	public abstract boolean isMainPlayer();

	@Inject(method = "tickMovement", at = @At("TAIL"))
	public void isSneaking(CallbackInfo ci) {
		final ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
		if (!this.isMainPlayer()) return;
		if (FSitClientMod.isInPose(PlayerPose.CRAWL)) player.setSwimming(true);

		if (this.prevSneaking && !this.input.sneaking) {
			if (FSitClientMod.isInPose(PlayerPose.CRAWL)) FSitClientMod.resetPose();
			else if (player.getPitch() >= FSitMod.config.minAngle) {
				if (FSitClientMod.isInPose(PlayerPose.SNEAK)) {
					if (player.isCrawling()) FSitClientMod.setCrawling();
					else FSitClientMod.setSitting(player.getPos());
				} else if (FSitMod.config.sneakSit) FSitClientMod.setSneaked();
			}
		}

		this.prevSneaking = this.input.sneaking;
	}
}
