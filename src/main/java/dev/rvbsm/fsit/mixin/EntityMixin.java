package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.packet.CrawlC2SPacket;
import dev.rvbsm.fsit.packet.SpawnSeatC2SPacket;
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

import java.util.UUID;


@Mixin(Entity.class)
public abstract class EntityMixin {

	@Shadow
	private World world;

	@Shadow
	public abstract boolean isOnGround();

	@Shadow
	public abstract boolean hasVehicle();

	@Shadow
	public abstract boolean isSpectator();

	@Shadow
	public abstract Vec3d getPos();

	@Inject(method = "setSneaking", at = @At(value = "HEAD"))
	public void setSneaking(boolean sneaking, CallbackInfo ci) {
		if (!this.isOnGround() || this.hasVehicle() || this.isSpectator()) return;

		if ((Entity) (Object) this instanceof final PlayerEntity player && !sneaking) {
			final UUID playerUid = player.getUuid();

			if (FSitMod.isCrawled(playerUid)) FSitMod.removeCrawled(playerUid);
			else if (player.getPitch() >= FSitMod.config.minAngle) {
				if (FSitMod.isSneaked(playerUid)) {
					if (player.isCrawling()) {
						if (player instanceof ServerPlayerEntity) FSitMod.addCrawled(player);
						else CrawlC2SPacket.send();
					}
					else {
						if (player instanceof ServerPlayerEntity) FSitMod.spawnSeat(player, this.world, this.getPos());
						else SpawnSeatC2SPacket.send(player.getPos(), null, false);
					}
				} else if (FSitMod.config.sneakSit) FSitMod.addSneaked(playerUid);
			}
		}
	}

	@Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At(value = "TAIL"))
	public void startRiding(Entity entity, boolean force, CallbackInfoReturnable<Boolean> cir) {
		if (entity instanceof ServerPlayerEntity player)
			player.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(entity));
	}

	/**
	 * @author <a href="https://github.com/ForwarD-NerN">ForwarD-NerN</a>
	 * @see <a href="https://github.com/ForwarD-NerN/PlayerLadder/blob/fc475d62fda188e09e3835cef4ba53b671931739/src/main/java/ru/nern/pladder/mixin/EntityMixin.java#L24-L33">source</a>
	 */
	@Inject(method = "removePassenger", at = @At(value = "TAIL"))
	protected void removePassenger(Entity passenger, CallbackInfo ci) {
		if ((Entity) (Object) this instanceof final ServerPlayerEntity player && passenger.isPlayer())
			player.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(player));
	}
}
