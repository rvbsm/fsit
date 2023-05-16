package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.FSitMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

	@Shadow
	public World world;

	@Shadow
	public abstract boolean isOnGround();

	@Shadow
	public abstract boolean hasVehicle();

	@Shadow
	public abstract boolean isSpectator();

	@Shadow
	public abstract Vec3d getPos();

	@Environment(EnvType.SERVER)
	@Inject(method = "setSneaking", at = @At(value = "HEAD"))
	public void setSneaking(boolean sneaking, CallbackInfo ci) {
		if (!this.isOnGround() || this.hasVehicle() || this.isSpectator()) return;

		if ((Entity) (Object) this instanceof final PlayerEntity player && !sneaking)
			if (FSitMod.isNeedSeat(player)) FSitMod.spawnSeat(player, this.world, this.getPos());
			else FSitMod.addSneaked(player);
	}

	@Environment(EnvType.SERVER)
	@Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At(value = "TAIL"))
	public void startRiding(Entity entity, boolean force, CallbackInfoReturnable<Boolean> cir) {
		if (entity instanceof ServerPlayerEntity player)
			player.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(entity));
	}

	/**
	 * @author <a href="https://github.com/ForwarD-NerN">ForwarD-NerN</a>
	 * @see <a href="https://github.com/ForwarD-NerN/PlayerLadder/blob/fc475d62fda188e09e3835cef4ba53b671931739/src/main/java/ru/nern/pladder/mixin/EntityMixin.java#L24-L33">source</a>
	 */
	@Environment(EnvType.SERVER)
	@Inject(method = "removePassenger", at = @At(value = "TAIL"))
	protected void removePassenger(Entity passenger, CallbackInfo ci) {
		if ((Entity) (Object) this instanceof final ServerPlayerEntity player && passenger.isPlayer())
			player.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(player));
	}
}
