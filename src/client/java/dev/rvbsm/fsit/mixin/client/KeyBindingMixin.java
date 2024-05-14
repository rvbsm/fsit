package dev.rvbsm.fsit.mixin.client;

import dev.rvbsm.fsit.client.option.FSitKeyBindings;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {
    @Inject(method = "untoggleStickyKeys", at = @At("TAIL"))
    private static void untoggleHybridKeys(CallbackInfo ci) {
        FSitKeyBindings.reset();
    }
}
