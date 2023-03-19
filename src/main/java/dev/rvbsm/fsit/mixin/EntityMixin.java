package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.FSitMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

	private final FSitMod FSit = FSitMod.getInstance();
	@Shadow public World world;

	@Shadow public abstract boolean isOnGround();

	@Shadow public abstract double getX();

	@Shadow public abstract double getY();

	@Shadow public abstract double getZ();

	@Shadow public abstract boolean hasVehicle();

	@Shadow public abstract boolean isSpectator();

	@Inject(method = "setSneaking", at = @At(value = "HEAD"))
	public void setSneaking(boolean sneaking, CallbackInfo ci) {
		if (this.world.isClient) return;
		if (!this.isOnGround() || this.hasVehicle() || this.isSpectator()) return;

		if ((Entity) (Object) this instanceof final PlayerEntity player) if (FSit.isNeedSeat(player) && !sneaking)
			FSit.spawnSeat(player, this.world, this.getX(), this.getY(), this.getZ());
		else FSit.addSneaked(player);
	}

	// https://github.com/ForwarD-NerN/PlayerLadder/blob/fc475d62fda188e09e3835cef4ba53b671931739/src/main/java/ru/nern/pladder/mixin/EntityMixin.java#L24-L33
	@Inject(method = "removePassenger", at = @At(value = "TAIL"))
	protected void removePassenger(Entity passenger, CallbackInfo ci) {
		if (this.world.isClient) return;
		if ((Entity) (Object) this instanceof final PlayerEntity player && passenger instanceof PlayerEntity)
			((ServerPlayerEntity) player).networkHandler.sendPacket(new EntityPassengersSetS2CPacket(player));
	}
}
