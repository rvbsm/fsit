package dev.rvbsm.fsit.mixin;

import dev.rvbsm.fsit.api.Poseable;
import dev.rvbsm.fsit.entity.Pose;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerEntity.class)
abstract public class PlayerEntityMixin extends LivingEntity implements Poseable {
    @Unique
    protected @NotNull Pose pose = Pose.Standing;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public abstract PlayerAbilities getAbilities();

    @ModifyVariable(method = "updatePose", at = @At("STORE"), ordinal = 1)
    private EntityPose updatePose(EntityPose pose) {
        if (this.fsit$isInPose(Pose.Crawling)) {
            return EntityPose.SWIMMING;
        }

        return pose;
    }

    @Override
    public void fsit$setPose(@NotNull Pose pose, @Nullable Vec3d pos) {
        this.pose = pose;
    }

    @Override
    public @NotNull Pose fsit$getPose() {
        return this.pose;
    }
}
