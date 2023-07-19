package dev.rvbsm.fsit.mixin.client;

import dev.rvbsm.fsit.FSitMod;
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
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

	@Shadow
	private Vec3d pos;

	@Shadow
	public abstract void calculateDimensions();

	@Shadow
	public abstract boolean isPlayer();

	@ModifyConstant(method = "getMountedHeightOffset", constant = @Constant(doubleValue = 0.75))
	public double getMountedHeightOffset$multiplier(double multiplier) {
		return this.isPlayer() ? 1.0 : multiplier;
	}

	@Inject(method = "getMountedHeightOffset", at = @At("TAIL"), cancellable = true)
	public void getMountedHeightOffset(CallbackInfoReturnable<Double> cir) {
		cir.setReturnValue(cir.getReturnValue() + FSitMod.getConfig().rideHeight);
	}

	@Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At("TAIL"))
	public void startRiding(Entity entity, boolean force, CallbackInfoReturnable<Boolean> cir) {
		if (this.isPlayer() && entity.isPlayer()) this.calculateDimensions();
	}

	@Redirect(method = "calculateDimensions", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getDimensions(Lnet/minecraft/entity/EntityPose;)Lnet/minecraft/entity/EntityDimensions;"))
	public EntityDimensions calculateDimensions$getDimensions(@NotNull Entity entity, EntityPose pose) {
		final EntityDimensions dimensions = entity.getDimensions(pose);
		return entity.isPlayer() && entity.getVehicle() instanceof PlayerEntity ? dimensions.scaled(1f, .8f) : dimensions;
	}

	@Redirect(method = "calculateBoundingBox", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;pos:Lnet/minecraft/util/math/Vec3d;", opcode = Opcodes.GETFIELD))
	protected Vec3d calculateBoundingBox$pos(@NotNull Entity entity) {
		if (entity.isPlayer() && entity.getVehicle() instanceof PlayerEntity) return this.pos.add(0d, .35d, 0d);
		else return this.pos;
	}
}
