package dev.rvbsm.fsit.mixin.client;

import com.mojang.authlib.GameProfile;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.entity.PlayerPose;
import dev.rvbsm.fsit.entity.PlayerPoseAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends PlayerEntity implements PlayerPoseAccessor {

	@Shadow
	@Final
	protected MinecraftClient client;
	@Unique
	private PlayerPose playerPose;

	public ClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@Inject(method = "tickMovement", at = @At("TAIL"))
	public void isSneaking(CallbackInfo ci) {
		final ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
		if (this.isInPose(PlayerPose.CRAWL)) player.setSwimming(true);
	}

	@Override
	public PlayerPose fsit$getPose() {
		return this.playerPose;
	}

	@Override
	public void fsit$setPose(PlayerPose pose) {
		this.playerPose = pose;

		if (this.isPosing())
			this.client.inGameHud.setOverlayMessage(FSitMod.getTranslation("message", "onpose", this.client.options.sneakKey.getBoundKeyLocalizedText()), false);
	}
}
