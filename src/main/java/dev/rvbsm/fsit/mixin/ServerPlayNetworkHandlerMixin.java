package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.entity.PlayerConfigAccessor;
import dev.rvbsm.fsit.entity.PlayerPose;
import dev.rvbsm.fsit.entity.PlayerPoseAccessor;
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
	public void onClientCommand(ClientCommandC2SPacket packet, CallbackInfo ci) {
		if (packet.getMode() != ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY) return;

		final PlayerPoseAccessor poseAccessor = (PlayerPoseAccessor) player;
		final ConfigData config = ((PlayerConfigAccessor) player).fsit$getConfig();

		if (!player.hasVehicle() && poseAccessor.isPosing()) poseAccessor.resetPose();
		else if (poseAccessor.isInPose(PlayerPose.NONE) && config.sneak) poseAccessor.fsit$setSneaked();
		else if (poseAccessor.isInPose(PlayerPose.SNEAK)) {
			if (player.getPitch() >= config.minAngle) {
				if (player.isCrawling()) poseAccessor.fsit$setCrawling();
				else poseAccessor.fsit$setSitting();
			} else if (player.getPitch() <= -config.minAngle) {
				if (player.getFirstPassenger() instanceof PlayerEntity passenger) passenger.stopRiding();
			}
		}
	}
}
