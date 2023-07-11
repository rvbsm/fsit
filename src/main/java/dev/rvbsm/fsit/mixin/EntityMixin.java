package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.entity.PlayerConfigAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Entity.class)
public abstract class EntityMixin {

	@Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At("TAIL"))
	public void startRiding(Entity entity, boolean force, CallbackInfoReturnable<Boolean> cir) {
		if (entity instanceof ServerPlayerEntity riddenPlayer) {
			riddenPlayer.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(riddenPlayer));
			if (!((PlayerConfigAccessor) riddenPlayer).fsit$isModded())
				riddenPlayer.sendMessage(Text.of("Look up and press Sneak key to dismount a player"), true);
		}
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
