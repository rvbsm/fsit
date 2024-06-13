package dev.rvbsm.fsit.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
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
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.network.EntityTrackerEntry;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntityMixin implements ConfigurableEntity, Crawlable, ServerPlayerClientVelocity {
    @Shadow
    public ServerPlayNetworkHandler networkHandler;

    @Unique
    private @Nullable ModConfig config;
    @Unique
    private @Nullable CrawlEntity crawlEntity;
    @Unique
    private boolean wasPassengerHidden = false;
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

        // note: at least it works fully server-side
        if (this.getFirstPassenger() instanceof ServerPlayerEntity passenger && this.fsit$getConfig().getRiding().getHideRider()) {
            this.playerPassengerTick(passenger);
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

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void dismountPlayer(Entity target, CallbackInfo ci) {
        if (this.hasPassenger(target) && target.isPlayer()) {
            target.stopRiding();
            ci.cancel();
        }
    }

    @Inject(method = "stopRiding", at = @At("TAIL"))
    private void resetPose(CallbackInfo ci, @Local Entity entity) {
        if (this.fsit$isInPose(PlayerPose.Sitting)) {
            this.fsit$resetPose();
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);

        if (passenger.isPlayer()) {
            this.wasPassengerHidden = false;
            this.showPlayerPassenger((PlayerEntity) passenger);
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
    public void fsit$setClientVelocity(Vec3d velocity) {
        this.clientVelocity = velocity;
    }

    @Override
    public Vec3d fsit$getClientVelocity() {
        return this.clientVelocity;
    }

    @Unique
    private void playerPassengerTick(@NotNull ServerPlayerEntity passenger) {
        final boolean hidePassenger = this.isSneaking() || this.getPitch() > 0;

        if (hidePassenger && !this.wasPassengerHidden) {
            this.hidePlayerPassenger(passenger);
        } else if (!hidePassenger && this.wasPassengerHidden) {
            this.showPlayerPassenger(passenger);
        }

        this.wasPassengerHidden = hidePassenger;
    }

    /**
     * @see EntityTrackerEntry#sendPackets(ServerPlayerEntity, Consumer)
     */
    @Unique
    private void showPlayerPassenger(@NotNull PlayerEntity player) {
        final List<Packet<ClientPlayPacketListener>> packets = new ArrayList<>();
        packets.add(new PlayerSpawnS2CPacket(player));

        final Collection<EntityAttributeInstance> attributes = player.getAttributes().getAttributesToSend();
        if (!attributes.isEmpty()) {
            packets.add(new EntityAttributesS2CPacket(player.getId(), attributes));
        }

        final List<Pair<EquipmentSlot, ItemStack>> equipment = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            final ItemStack itemStack = player.getEquippedStack(slot);
            if (!itemStack.isEmpty()) {
                equipment.add(Pair.of(slot, itemStack.copy()));
            }
        }

        if (!equipment.isEmpty()) {
            packets.add(new EntityEquipmentUpdateS2CPacket(player.getId(), equipment));
        }

        if (player.hasPassengers()) {
            packets.add(new EntityPassengersSetS2CPacket(player));
        }

        if (player.getVehicle() != null) {
            packets.add(new EntityPassengersSetS2CPacket(this));
        }

        this.networkHandler.sendPacket(new BundleS2CPacket(packets));
    }

    @Unique
    private void hidePlayerPassenger(@NotNull PlayerEntity player) {
        this.networkHandler.sendPacket(new EntitiesDestroyS2CPacket(player.getId()));
    }
}
