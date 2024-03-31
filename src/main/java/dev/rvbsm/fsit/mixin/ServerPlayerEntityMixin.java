package dev.rvbsm.fsit.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.api.ConfigurableEntity;
import dev.rvbsm.fsit.api.Crawlable;
import dev.rvbsm.fsit.config.ModConfig;
import dev.rvbsm.fsit.entity.CrawlEntity;
import dev.rvbsm.fsit.entity.Pose;
import dev.rvbsm.fsit.event.UpdatePoseCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntityMixin implements ConfigurableEntity, Crawlable {
    @Shadow
    public ServerPlayNetworkHandler networkHandler;

    @Unique
    private @Nullable ModConfig config;
    @Unique
    private @Nullable CrawlEntity crawlEntity;
    @Unique
    private boolean wasPassengerHidden = false;

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

        // note: at least it works fully server-side
        this.playerPassengerTick();
    }

    @Inject(method = "onDisconnect", at = @At("TAIL"))
    private void dismountSeat(CallbackInfo ci) {
        if (this.fsit$isInPose(Pose.Sitting)) {
            this.stopRiding();
        }
    }


    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void dismountPlayer(Entity target, CallbackInfo ci) {
        if (this.hasPassenger(target) && target.isPlayer()) {
            target.stopRiding();
            ci.cancel();
        }
    }

    @Inject(method = "stopRiding", at = @At("TAIL"))
    private void resetPose(CallbackInfo ci, @Local Entity entity) {
        if (this.fsit$isInPose(Pose.Sitting)) {
            this.fsit$resetPose();
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);

        if (passenger.isPlayer()) {
            if (this.wasPassengerHidden) {
                this.wasPassengerHidden = false;
                this.networkHandler.sendPacket(passenger.createSpawnPacket());
            }
            this.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(this));
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
    public void fsit$setPose(@NotNull Pose pose, @Nullable Vec3d pos) {
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

    @Unique
    private void playerPassengerTick() {
        final ServerPlayerEntity passenger = (ServerPlayerEntity) this.getFirstPassenger();
        if (passenger != null) {
            final boolean hidePassenger = this.isSneaking() || this.getPitch() > 0; // todo

            if (hidePassenger && !this.wasPassengerHidden) {
                this.networkHandler.sendPacket(new EntitiesDestroyS2CPacket(passenger.getId()));
            } else if (!hidePassenger && this.wasPassengerHidden) {
                this.networkHandler.sendPacket(passenger.createSpawnPacket());
                this.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(this));
            }

            this.wasPassengerHidden = hidePassenger;
        }
    }
}
