package dev.rvbsm.fsit.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.ConfigurableEntity;
import dev.rvbsm.fsit.api.Crawlable;
import dev.rvbsm.fsit.api.ServerPlayerClientVelocity;
import dev.rvbsm.fsit.config.ModConfig;
import dev.rvbsm.fsit.entity.CrawlEntity;
import dev.rvbsm.fsit.entity.PlayerPose;
import dev.rvbsm.fsit.event.UpdatePoseCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntityMixin implements ConfigurableEntity, Crawlable, ServerPlayerClientVelocity {
    @Unique
    private @Nullable ModConfig config;
    @Unique
    private @Nullable CrawlEntity crawlEntity;
    @Unique
    private Vec3d clientVelocity = Vec3d.ZERO;

    protected ServerPlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "playerTick", at = @At("TAIL"))
    private void tickCrawlingVanillaPlayer(CallbackInfo ci) {
        if (this.fsit$isInPose()) {
            if (this.getAbilities().flying || this.isSneaking()) {
                this.fsit$resetPose();
            }

            if (this.crawlEntity != null) {
                this.crawlEntity.tick();
            }
        }
    }

    @Inject(method = "onDisconnect", at = @At("TAIL"))
    private void dismountSeat(CallbackInfo ci) {
        if (this.fsit$isInPose(PlayerPose.Sitting)) {
            this.stopRiding();
        }
    }

    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void copyConfig(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        final ConfigurableEntity configurablePlayer = (ConfigurableEntity) oldPlayer;
        if (configurablePlayer.fsit$hasConfig()) {
            this.config = configurablePlayer.fsit$getConfig();
        }
    }

    @Inject(method = "stopRiding", at = @At("TAIL"))
    private void resetPose(CallbackInfo ci, @Local Entity entity) {
        if (this.fsit$isInPose(PlayerPose.Sitting)) {
            this.fsit$resetPose();
        }
    }

    @Override
    public boolean hasPlayerRider() {
        return false;
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        return this.getPos();
    }

    @Override
    public void fsit$setConfig(@NotNull ModConfig config) {
        this.config = config;
    }

    @Override
    public @NotNull ModConfig fsit$getConfig() {
        if (FSitMod.getConfig().getUseServer() || this.config == null || this.config.getUseServer()) {
            return FSitMod.getConfig();
        }

        return this.config;
    }

    @Override
    public boolean fsit$hasConfig() {
        return this.config != null;
    }

    @Override
    public void fsit$setPose(@NotNull PlayerPose pose, @Nullable Vec3d pos) {
        super.fsit$setPose(pose, pos);

        UpdatePoseCallback.EVENT.invoker().onUpdatePose((ServerPlayerEntity) (Object) this, pose, pos);
    }

    @Override
    public void fsit$startCrawling(@NotNull CrawlEntity crawlEntity) {
        this.crawlEntity = crawlEntity;
    }

    @Override
    public void fsit$stopCrawling() {
        if (this.crawlEntity != null) {
            this.crawlEntity.discard();
            this.crawlEntity = null;
        }
    }

    @Override
    public boolean fsit$isCrawling() {
        return this.crawlEntity != null && !this.crawlEntity.isRemoved();
    }

    @Override
    public void fsit$setClientVelocity(@NotNull Vec3d velocity) {
        this.clientVelocity = velocity;
    }

    @Override
    public @NotNull Vec3d fsit$getClientVelocity() {
        return this.clientVelocity;
    }
}
