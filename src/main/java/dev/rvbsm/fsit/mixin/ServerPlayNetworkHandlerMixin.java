package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.entity.ConfigHandler;
import dev.rvbsm.fsit.entity.PlayerPose;
import dev.rvbsm.fsit.entity.PoseHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

	@Shadow
	public ServerPlayerEntity player;

	@Inject(method = "onClientCommand", at = @At("TAIL"))
	public void onClientCommand$catchSneak(ClientCommandC2SPacket packet, CallbackInfo ci) {
		if (packet.getMode() != ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY) return;

		final PoseHandler poseHandler = (PoseHandler) player;
		final ConfigData config = ((ConfigHandler) player).fsit$getConfig();
		final ConfigData.SneakTable configSneak = config.getSneak();

		if (!player.hasVehicle() && poseHandler.isPosing()) poseHandler.resetPose();
		else if (poseHandler.isInPose(PlayerPose.NONE)) poseHandler.fsit$setSneaked();
		else if (poseHandler.isInPose(PlayerPose.SNEAK)) {
			if (player.getPitch() >= configSneak.getAngle() && configSneak.isEnabled()) {
				if (player.isCrawling()) poseHandler.fsit$setCrawling();
				else poseHandler.fsit$setSitting();
			} else if (player.getPitch() <= 0) {
				if (player.getFirstPassenger() instanceof PlayerEntity passenger) passenger.stopRiding();
			}
		}
	}
}
