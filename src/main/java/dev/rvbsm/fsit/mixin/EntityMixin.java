package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.entity.ConfigHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
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
	public abstract World getWorld();

	@Shadow
	public abstract boolean isPlayer();

	@Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At("TAIL"))
	public void startRiding$updateRidden(Entity entity, boolean force, CallbackInfoReturnable<Boolean> cir) {
		if (entity.isPlayer() && !this.getWorld().isClient) {
			((ServerPlayerEntity) entity).networkHandler.sendPacket(new EntityPassengersSetS2CPacket(entity));
			if (!((ConfigHandler) entity).fsit$hasConfig())
				((PlayerEntity) entity).sendMessage(Text.of("Look up and press Sneak key to dismount a player"), true);
		}
	}

	/**
	 * @author <a href="https://github.com/ForwarD-NerN">ForwarD-NerN</a>
	 * @see <a href="https://github.com/ForwarD-NerN/PlayerLadder/blob/fc475d62fda188e09e3835cef4ba53b671931739/src/main/java/ru/nern/pladder/mixin/EntityMixin.java#L24-L33">https://github.com/ForwarD-NerN/PlayerLadder</a>
	 */
	@Inject(method = "removePassenger", at = @At("TAIL"))
	protected void removePassenger$removeRider(Entity passenger, CallbackInfo ci) {
		if (this.getWorld().isClient && this.isPlayer() && passenger.isPlayer())
			((ServerPlayerEntity) (Object) this).networkHandler.sendPacket(new EntityPassengersSetS2CPacket((Entity) (Object) this));
	}
}
