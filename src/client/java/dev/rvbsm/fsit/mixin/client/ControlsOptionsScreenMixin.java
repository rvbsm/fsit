package dev.rvbsm.fsit.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import dev.rvbsm.fsit.client.FSitModClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(ControlsOptionsScreen.class)
public abstract class ControlsOptionsScreenMixin extends GameOptionsScreen {
    public ControlsOptionsScreenMixin(Screen parent, GameOptions gameOptions, Text title) {
        super(parent, gameOptions, title);
    }

    /*? if <=1.20.4 {*/
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/option/ControlsOptionsScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;", ordinal = 3))
    private void fsitOptions(CallbackInfo ci, @Local(ordinal = 0) int i, @Local(ordinal = 1) int j, @Local(ordinal = 2) LocalIntRef k) {
        k.set(k.get() + 24);
        this.addDrawableChild(FSitModClient.getSitMode().createWidget(this.gameOptions, i, k.get(), 150));
        this.addDrawableChild(FSitModClient.getCrawlMode().createWidget(this.gameOptions, j, k.get(), 150));
    }
    /*?} else {*//*
    @Inject(method = "getOptions", at = @At("RETURN"), cancellable = true)
    private static void fsitOptions(GameOptions gameOptions, CallbackInfoReturnable<net.minecraft.client.option.SimpleOption<?>[]> cir) {
        net.minecraft.client.option.SimpleOption<?>[] options = cir.getReturnValue();
        options = org.apache.commons.lang3.ArrayUtils.insert(2, options, FSitModClient.getSitMode());
        options = org.apache.commons.lang3.ArrayUtils.insert(3, options, FSitModClient.getCrawlMode());

        cir.setReturnValue(options);
    }
    *//*?} */
}
