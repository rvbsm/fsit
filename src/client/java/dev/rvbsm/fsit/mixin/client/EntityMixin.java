package dev.rvbsm.fsit.mixin.client;

import dev.rvbsm.fsit.FSitClientMod;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.packet.CrawlC2SPacket;
import dev.rvbsm.fsit.packet.SpawnSeatC2SPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public abstract class EntityMixin {

	@Shadow
	private Vec3d pos;

	@Shadow
	public abstract void calculateDimensions();

	@Shadow
	public abstract boolean isPlayer();

	@Shadow
	public abstract boolean isOnGround();

	@Shadow
	public abstract boolean hasVehicle();

	@Shadow
	public abstract boolean isSpectator();

	@Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At(value = "TAIL"))
	public void startRiding(Entity entity, boolean force, CallbackInfoReturnable<Boolean> cir) {
		if (this.isPlayer() && entity.isPlayer()) this.calculateDimensions();
	}

	@Redirect(method = "updatePassengerPosition(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity$PositionUpdater;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getHeightOffset()D"))
	private double updatePassengerPosition$getHeightOffset(@NotNull Entity passenger) {
		if (passenger.getVehicle() instanceof PlayerEntity vehicle) return vehicle.isInSwimmingPose() ? -0.31d : 0d;

		return passenger.getHeightOffset();
	}

	@Redirect(method = "calculateDimensions", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getDimensions(Lnet/minecraft/entity/EntityPose;)Lnet/minecraft/entity/EntityDimensions;"))
	public EntityDimensions calculateDimensions$getDimensions(@NotNull Entity entity, EntityPose pose) {
		final EntityDimensions dimensions = entity.getDimensions(pose);
		return entity.isPlayer() && entity.getVehicle() instanceof PlayerEntity ? dimensions.scaled(1f, .75f) : dimensions;
	}

	@Redirect(method = "calculateBoundingBox", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;pos:Lnet/minecraft/util/math/Vec3d;", opcode = Opcodes.GETFIELD))
	protected Vec3d calculateBoundingBox$pos(@NotNull Entity entity) {
		if (entity.isPlayer() && entity.getVehicle() instanceof PlayerEntity) return this.pos.add(0d, .47d, 0d);
		else return this.pos;
	}

	@Inject(method = "setSneaking", at = @At("HEAD"))
	public void setSneaking(boolean sneaking, CallbackInfo ci) {
		if (!this.isOnGround() || this.hasVehicle() || this.isSpectator()) return;
		else if (!MinecraftClient.getInstance().player.equals(this)) return;

		if ((Entity) (Object) this instanceof final PlayerEntity player && !sneaking) {
			if (player.getPitch() >= FSitMod.config.minAngle) {
				if (FSitClientMod.isSneaked()) {
					if (player.isCrawling()) CrawlC2SPacket.send();
					else SpawnSeatC2SPacket.send(player.getPos(), null);
				} else if (FSitMod.config.sneakSit) FSitClientMod.addSneaked();
			}
		}
	}
}
