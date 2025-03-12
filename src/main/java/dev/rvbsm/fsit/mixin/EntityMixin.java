package dev.rvbsm.fsit.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
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

    @Inject(method = "move", at = @At("TAIL"))
    protected void onMove(MovementType type, Vec3d movement, CallbackInfo ci) {
    }

    @ModifyReturnValue(method = "hasPlayerRider", at = @At("RETURN"))
    protected boolean hasPlayerRider(boolean original) {
        return original;
    }
}
