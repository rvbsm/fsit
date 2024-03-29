package dev.rvbsm.fsit.mixin.client;

import dev.rvbsm.fsit.client.FSitModClient;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
abstract public class GameOptionsMixin {

    @Inject(method = "accept", at = @At("TAIL"))
    private void accept(GameOptions.Visitor visitor, CallbackInfo ci) {
        visitor.accept("fsit.sitMode", FSitModClient.getSitKeyMode());
        visitor.accept("fsit.crawlMode", FSitModClient.getCrawlKeyMode());
    }
}
