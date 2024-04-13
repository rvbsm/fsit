package dev.rvbsm.fsit.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import dev.rvbsm.fsit.client.FSitModClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.option.*;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(ControlsOptionsScreen.class)
abstract public class ControlsOptionScreenMixin extends GameOptionsScreen {
    public ControlsOptionScreenMixin(Screen parent, GameOptions gameOptions, Text title) {
        super(parent, gameOptions, title);
    }

    /*? if >=1.20.5- {*//*
    @Redirect(method = "getOptions", at = @At("RETURN"))
    private static SimpleOption<?>[] withFSitOptions(GameOptions gameOptions, CallbackInfoReturnable<SimpleOption<?>[]> cir) {
        final java.util.List<SimpleOption<?>> options = java.util.Arrays.asList(cir.getReturnValue());
        options.add(2, FSitModClient.getSitKeyMode());
        options.add(3, FSitModClient.getCrawlKeyMode());
        return options.toArray(SimpleOption[]::new);
    }
    *//*?} else {*/
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/option/ControlsOptionsScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;", ordinal = 3))
    private void init(CallbackInfo ci, @Local(ordinal = 0) int i, @Local(ordinal = 1) int j, @Local(ordinal = 2) LocalIntRef k) {
        k.set(k.get() + 24);
        this.addDrawableChild(FSitModClient.getSitKeyMode().createWidget(this.gameOptions, i, k.get(), 150));
        this.addDrawableChild(FSitModClient.getCrawlKeyMode().createWidget(this.gameOptions, j, k.get(), 150));
    }
    /*?} */
}
