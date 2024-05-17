package dev.rvbsm.fsit.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.rvbsm.fsit.api.ServerPlayerClientVelocity;
import dev.rvbsm.fsit.event.ClientCommandCallback;
import dev.rvbsm.fsit.event.PassedUseBlockCallback;
import dev.rvbsm.fsit.event.PassedUseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onClientCommand", at = @At("TAIL"))
    public void onClientCommand(@NotNull ClientCommandC2SPacket packet, CallbackInfo ci) {
        ClientCommandCallback.EVENT.invoker().onClientMode(this.player, packet.getMode());
    }

    @ModifyVariable(method = "onPlayerInteractBlock", at = @At("STORE"))
    private ActionResult interactBlock(ActionResult interactionActionResult, @Local ServerWorld world, @Local LocalRef<Hand> handRef, @Local BlockHitResult blockHitResult) {
        if (handRef.get() == Hand.OFF_HAND && interactionActionResult == ActionResult.PASS) {
            handRef.set(Hand.MAIN_HAND);

            return PassedUseBlockCallback.EVENT.invoker().interactBlock(player, world, blockHitResult);
        }

        return interactionActionResult;
    }

    @ModifyArg(method = "onPlayerMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"), index = 1)
    private Vec3d captureClientVelocity(Vec3d movement) {
        ((ServerPlayerClientVelocity) this.player).fsit$setClientVelocity(movement);

        return movement;
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

        // note: idk why there are errors here. mcdev being dumb
        @ModifyVariable(method = "processInteract", at = @At("STORE"))
        private ActionResult interactPlayer(ActionResult interactionActionResult, @Local LocalRef<Hand> handRef) {
            if (handRef.get() == Hand.OFF_HAND && interactionActionResult == ActionResult.PASS) {
                handRef.set(Hand.MAIN_HAND);

                return PassedUseEntityCallback.EVENT.invoker().interactEntity(field_28963.player, field_39991, field_28962);
            }

            return interactionActionResult;
        }
    }
}
