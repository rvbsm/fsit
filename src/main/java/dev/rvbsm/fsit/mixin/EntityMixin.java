package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.entity.PlayerPose;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;


@Mixin(Entity.class)
public abstract class EntityMixin {

	@Shadow
	public abstract Vec3d getPos();

	@Inject(method = "setSneaking", at = @At("HEAD"))
	public void setSneaking(boolean sneaking, CallbackInfo ci) {
		if ((Entity) (Object) this instanceof final ServerPlayerEntity player && !sneaking) {
			final UUID playerId = player.getUuid();
			final ConfigData config = FSitMod.getConfig(playerId);

			if (FSitMod.isPosing(playerId)) FSitMod.resetPose(player);
			else if (FSitMod.isInPose(playerId, PlayerPose.NONE) && config.sneak) FSitMod.setSneaked(player);
			else if (FSitMod.isInPose(playerId, PlayerPose.SNEAK)) {
				if (player.getPitch() >= config.minAngle) {
					if (player.isCrawling()) FSitMod.setCrawling(player);
					else FSitMod.setSitting(player, player.getPos());
				} else if (player.getPitch() <= -config.minAngle) {
					if (player.getFirstPassenger() instanceof PlayerEntity passenger) passenger.stopRiding();
				}
			}
		}
	}

	@Inject(method = "stopRiding", at = @At("HEAD"))
	public void stopRiding(CallbackInfo ci) {
		if ((Entity) (Object) this instanceof ServerPlayerEntity player)
			if (FSitMod.isInPose(player.getUuid(), PlayerPose.SIT)) FSitMod.resetPose(player);
	}

	@Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At("TAIL"))
	public void startRiding(Entity entity, boolean force, CallbackInfoReturnable<Boolean> cir) {
		if (entity instanceof ServerPlayerEntity player)
			player.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(entity));
	}

	/**
	 * @author <a href="https://github.com/ForwarD-NerN">ForwarD-NerN</a>
	 * @see <a href="https://github.com/ForwarD-NerN/PlayerLadder/blob/fc475d62fda188e09e3835cef4ba53b671931739/src/main/java/ru/nern/pladder/mixin/EntityMixin.java#L24-L33">https://github.com/ForwarD-NerN/PlayerLadder</a>
	 */
	@Inject(method = "removePassenger", at = @At("TAIL"))
	protected void removePassenger(Entity passenger, CallbackInfo ci) {
		if ((Entity) (Object) this instanceof final ServerPlayerEntity player && passenger.isPlayer())
			player.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(player));
	}
}
