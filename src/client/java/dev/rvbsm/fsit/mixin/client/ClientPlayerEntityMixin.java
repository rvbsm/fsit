package dev.rvbsm.fsit.mixin.client;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.entity.PlayerPose;
import dev.rvbsm.fsit.mixin.PlayerEntityMixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends PlayerEntityMixin {

	@Shadow
	@Final
	protected MinecraftClient client;

	protected ClientPlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Override
	public void fsit$setPose(PlayerPose pose) {
		super.fsit$setPose(pose);

		if (this.isPosing())
			this.client.inGameHud.setOverlayMessage(FSitMod.getTranslation("message", "onpose", this.client.options.sneakKey.getBoundKeyLocalizedText()), false);
	}
}
