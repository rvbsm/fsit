package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.entity.SeatEntity;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AreaEffectCloudEntity.class)
public abstract class AreaEffectCloudEntityMixin extends Entity {

	public AreaEffectCloudEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void tick$cleanSit(CallbackInfo ci) {
		if (this.age > 20 && !this.hasPassengers() && this.getCustomName() != null && this.getCustomName().equals(SeatEntity.CUSTOM_NAME))
			this.discard();
	}
}
