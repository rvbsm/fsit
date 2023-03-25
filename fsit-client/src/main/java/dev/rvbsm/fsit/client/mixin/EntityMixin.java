package dev.rvbsm.fsit.client.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

	@Shadow private EntityDimensions dimensions;
	@Shadow private Vec3d pos;

	@Shadow
	public abstract void calculateDimensions();

	@Shadow
	public abstract boolean isPlayer();

	// https://github.com/ForwarD-NerN/PlayerLadder/blob/fc475d62fda188e09e3835cef4ba53b671931739/src/main/java/ru/nern/pladder/mixin/EntityMixin.java#L35-L41
	@Redirect(method = "updatePassengerPosition(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity$PositionUpdater;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getMountedHeightOffset()D"))
	private double getMountedHeightOffset(@NotNull Entity entity) {
		return entity instanceof PlayerEntity && (entity.getFirstPassenger() instanceof PlayerEntity || entity.getVehicle() instanceof PlayerEntity)
		       ? dimensions.height * .93d : entity.getMountedHeightOffset();
	}

	@Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At(value = "TAIL"))
	public void startRiding(Entity entity, boolean force, CallbackInfoReturnable<Boolean> cir) {
		if (this.isPlayer() && entity.isPlayer()) this.calculateDimensions();
	}

	@Redirect(method = "calculateDimensions", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getDimensions(Lnet/minecraft/entity/EntityPose;)Lnet/minecraft/entity/EntityDimensions;"))
	public EntityDimensions getDimensions(@NotNull Entity entity, EntityPose pose) {
		final EntityDimensions dimensions = entity.getDimensions(pose);
		return entity.isPlayer() && entity.getVehicle() instanceof PlayerEntity
		       ? dimensions.scaled(1f, .75f)
		       : dimensions;
	}

	@Redirect(method = "calculateBoundingBox", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;pos:Lnet/minecraft/util/math/Vec3d;", opcode = Opcodes.GETFIELD))
	protected Vec3d calculateBoundingBox(@NotNull Entity entity) {
		if (entity.isPlayer() && entity.getVehicle() instanceof PlayerEntity) return this.pos.add(0d, .48d, 0d);
		else return this.pos;
	}
}
