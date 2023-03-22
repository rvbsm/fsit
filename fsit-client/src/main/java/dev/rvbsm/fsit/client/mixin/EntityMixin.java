package dev.rvbsm.fsit.client.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityMixin {

	@Shadow private EntityDimensions dimensions;

	// https://github.com/ForwarD-NerN/PlayerLadder/blob/fc475d62fda188e09e3835cef4ba53b671931739/src/main/java/ru/nern/pladder/mixin/EntityMixin.java#L35-L41
	@Redirect(method = "updatePassengerPosition(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity$PositionUpdater;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getMountedHeightOffset()D"))
	private double getMountedHeightOffset(@NotNull Entity entity) {
		if (!entity.world.isClient) return entity.getMountedHeightOffset();
		return entity instanceof PlayerEntity && (entity.getFirstPassenger() instanceof PlayerEntity || entity.getVehicle() instanceof PlayerEntity)
		       ? dimensions.height * 0.93 : entity.getMountedHeightOffset();
	}
}
