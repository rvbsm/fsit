package dev.rvbsm.fsit.mixin;

import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import dev.rvbsm.fsit.api.player.PlayerPose;
import dev.rvbsm.fsit.entity.ModPose;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin implements PlayerPose {

    @Unique
    protected @NotNull ModPose modPose = ModPose.Standing;

    @Unique
    private @Nullable ModPose prevModPose = null;

    @Shadow
    public abstract PlayerAbilities getAbilities();

    @ModifyExpressionValue(
        method = "updatePose",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSwimming()Z"))
    private boolean isCrawling(boolean isSwimming) {
        return isSwimming || this.fsit$isInPose(ModPose.Crawling);
    }

    @Override
    public void fsit$setPose(@NotNull ModPose pose, @Nullable Vec3d pos) {
        this.prevModPose = this.modPose;
        this.modPose = pose;
    }

    @Override
    public @NotNull ModPose fsit$getPose() {
        return this.modPose;
    }

    @Override
    public @Nullable ModPose fsit$getPrevPose() {
        return this.prevModPose;
    }
}
