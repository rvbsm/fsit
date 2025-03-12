package dev.rvbsm.fsit.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin {

    @Shadow
    public abstract void stopRiding();

    @Inject(method = "onDismounted", at = @At("TAIL"))
    protected void onDismount(Entity vehicle, CallbackInfo ci) {
    }
}
