package dev.rvbsm.fsit.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.rvbsm.fsit.api.event.PassedUseBlockCallback;
import dev.rvbsm.fsit.api.event.PassedUseEntityCallback;
import dev.rvbsm.fsit.api.network.RidingRequestHandler;
import dev.rvbsm.fsit.api.player.PlayerConfig;
import dev.rvbsm.fsit.api.player.PlayerLastSneakTime;
import dev.rvbsm.fsit.api.player.PlayerPose;
import dev.rvbsm.fsit.entity.ModPose;
import dev.rvbsm.fsit.networking.payload.RidingResponseC2SPayload;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin implements RidingRequestHandler {

    @Unique
    private final Map<UUID, CompletableFuture<Boolean>> pendingRidingRequests = new WeakHashMap<>();
    @Shadow
    public ServerPlayerEntity player;

    //? if <=1.21.5 {
    @Inject(method = "onClientCommand", at = @At("TAIL"))
    public void onClientCommand(net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket packet, CallbackInfo ci) {
        dev.rvbsm.fsit.api.event.ClientCommandCallback.EVENT.invoker().process(this.player, packet.getMode());
    }
    //?}

    // just a copy-paste from SneakListener
    //? if >=1.21.6 {
    /*@ModifyArg(
        method = "onPlayerInput",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setSneaking(Z)V"))
    public boolean onPlayerInput(boolean sneaking) {
        if (sneaking && this.updateSneaking()) {
            return false;
        }

        return sneaking;
    }

    @Unique
    private boolean updateSneaking() {
        final var playerSneak = (PlayerLastSneakTime) this.player;
        final var sneakConfig = ((PlayerConfig) this.player).fsit$getConfig().getOnSneak();
        if (!sneakConfig.getSitting() && !sneakConfig.getCrawling()) {
            return false;
        } else if (this.player.getPitch() < sneakConfig.getMinPitch()) {
            return false;
        }

        if (this.player.hasVehicle() || !this.player.isOnGround()) {
            return false;
        }

        if (Util.getMeasuringTimeMs() - playerSneak.fsit$getLastSneakTime() <= sneakConfig.getDelay()) {
            final ModPose modPose;
            if (sneakConfig.getCrawling() && this.isPlayerNearGap()) {
                modPose = ModPose.Crawling;
            } else if (sneakConfig.getSitting()) {
                modPose = ModPose.Sitting;
            } else {
                return false;
            }

            playerSneak.fsit$resetLastSneakTime();
            ((PlayerPose) this.player).fsit$setPose(modPose, this.player.getPos());
            return true;
        }

        playerSneak.fsit$updateLastSneakTime();
        return false;
    }

    @Unique
    private boolean isPlayerNearGap() {
        final EntityDimensions crawlingDimensions = this.player.getDimensions(EntityPose.SWIMMING);
        final EntityDimensions crouchingDimensions = this.player.getDimensions(EntityPose.CROUCHING);

        final double yawRadians = this.player.getYaw() / 180 * Math.PI;
        final double offsetX = -Math.sin(yawRadians) * 0.1;
        final double offsetZ = Math.cos(yawRadians) * 0.1;

        final Vec3d playerPos = this.player.getPos();
        final Vec3d expectEmptyAt = playerPos.add(offsetX, 0.0, offsetZ);
        final Vec3d expectFullAt = playerPos.add(offsetX, crouchingDimensions.height(), offsetZ);

        final World world = this.player.getWorld();
        return world.isSpaceEmpty(this.player, crawlingDimensions.getBoxAt(expectEmptyAt).contract(1.0e-6)) &&
            !world.isSpaceEmpty(this.player, crawlingDimensions.getBoxAt(expectFullAt).contract(1.0e-6));
    }
    *///?}

    @ModifyVariable(method = "onPlayerInteractBlock", at = @At("STORE"))
    private ActionResult interactBlock(
        ActionResult interactionActionResult,
        @Local ServerWorld world,
        @Local LocalRef<Hand> handRef,
        @Local BlockHitResult blockHitResult
    ) {
        if (interactionActionResult == ActionResult.PASS &&
            handRef.get() == Hand.OFF_HAND &&
            player.getStackInHand(handRef.get()).getUseAction().ordinal() == 0) {
            handRef.set(Hand.MAIN_HAND);

            return PassedUseBlockCallback.EVENT.invoker().interact(player, world, blockHitResult);
        }

        return interactionActionResult;
    }

    @Inject(method = "onDisconnected", at = @At("TAIL"))
    public void purgePendingRequests(CallbackInfo ci) {
        this.pendingRidingRequests.forEach((uuid, future) -> future.complete(false));
        this.pendingRidingRequests.clear();
    }

    @Override
    public @NotNull CompletableFuture<Boolean> fsit$newRidingRequest(
        @NotNull UUID playerUUID,
        @NotNull Duration timeout
    ) {
        final CompletableFuture<Boolean> pendingFuture = this.pendingRidingRequests.get(playerUUID);
        if (pendingFuture != null && !pendingFuture.isDone()) {
            return CompletableFuture.completedFuture(false);
        }

        final CompletableFuture<Boolean> ridingResponse = new CompletableFuture<Boolean>().completeOnTimeout(
            false,
            timeout.toMillis(),
            TimeUnit.MILLISECONDS);
        this.pendingRidingRequests.put(playerUUID, ridingResponse);

        return ridingResponse;
    }

    @Override
    public void fsit$completeRidingRequest(@NotNull RidingResponseC2SPayload response) {
        final CompletableFuture<Boolean> future = this.pendingRidingRequests.remove(response.getUuid());
        if (future != null && !future.isDone()) {
            future.complete(response.getResponse().isAccepted());
        }
    }

    @Mixin(targets = "net.minecraft.server.network.ServerPlayNetworkHandler$1")
    public abstract static class PlayerInteractEntityC2SPacketHandler {
        @Shadow
        @Final
        ServerPlayNetworkHandler field_28963;

        @Shadow
        @Final
        Entity field_28962;

        @Shadow
        @Final
        ServerWorld field_39991;

        @ModifyVariable(method = "processInteract", at = @At("STORE"))
        private ActionResult interactPlayer(
            ActionResult interactionActionResult,
            @Local(argsOnly = true) LocalRef<Hand> handRef
        ) {
            if (interactionActionResult == ActionResult.PASS &&
                handRef.get() == Hand.OFF_HAND &&
                field_28963.player.getStackInHand(handRef.get()).getUseAction().ordinal() == 0) {
                handRef.set(Hand.MAIN_HAND);

                return PassedUseEntityCallback.EVENT.invoker().interact(field_28963.player, field_39991, field_28962);
            }

            return interactionActionResult;
        }
    }
}
