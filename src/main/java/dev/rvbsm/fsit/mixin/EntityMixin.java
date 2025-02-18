package dev.rvbsm.fsit.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public abstract boolean isSneaking();

    @Shadow
    public abstract Vec3d getPos();

    @Inject(
        method = "move",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setPosition(DDD)V", ordinal = 1))
    protected void move(
        CallbackInfo ci,
        @Local(ordinal = 1) Vec3d velocity
    ) {
    }

    @ModifyReturnValue(method = "hasPlayerRider", at = @At("RETURN"))
    protected boolean hasPlayerRider(boolean original) {
        return original;
    }

    @ModifyReturnValue(method = "updatePassengerForDismount", at = @At("RETURN"))
    protected Vec3d updatePassengerForDismount(Vec3d original, @Local(argsOnly = true) LivingEntity passenger) {
        return original;
    }
}
