package dev.rvbsm.fsit.mixin.client;

import net.minecraft.client.option.GameOptions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.rvbsm.fsit.client.FSitModClient;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {

    @Inject(method = "accept", at = @At("TAIL"))
    private void accept(GameOptions.Visitor visitor, CallbackInfo ci) {
        visitor.accept("fsit.sitMode", FSitModClient.INSTANCE.getSitMode());
        visitor.accept("fsit.crawlMode", FSitModClient.INSTANCE.getCrawlMode());
    }
}
