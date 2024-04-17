package dev.rvbsm.fsit.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
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
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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

    @Mixin(targets = "net.minecraft.server.network.ServerPlayNetworkHandler$1")
    public static abstract class PlayerInteractEntityC2SPacketHandler {
        @Shadow(aliases = "field_28963")
        @Final
        ServerPlayNetworkHandler this$ServerPlayNetworkHandler;

        @Shadow(aliases = "field_28962")
        @Final
        Entity entity;

        @Shadow(aliases = "field_39991")
        @Final
        ServerWorld world;

        // note: idk why there are errors here. mcdev being dumb
        @ModifyVariable(method = "processInteract", at = @At("STORE"))
        private ActionResult interactPlayer(ActionResult interactionActionResult, @Local LocalRef<Hand> handRef) {
            if (handRef.get() == Hand.OFF_HAND && interactionActionResult == ActionResult.PASS) {
                handRef.set(Hand.MAIN_HAND);

                return PassedUseEntityCallback.EVENT.invoker().interactEntity(this$ServerPlayNetworkHandler.player, world, entity);
            }

            return interactionActionResult;
        }
    }
}
