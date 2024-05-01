package dev.rvbsm.fsit.mixin.client;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.client.FSitModClient;
import dev.rvbsm.fsit.client.config.RestrictionList;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

@Mixin(SocialInteractionsPlayerListEntry.class)
public abstract class SocialInteractionsPlayerListEntryMixin extends ElementListWidget.Entry<SocialInteractionsPlayerListEntry> {
    /*? if <=1.20.1 {*//*
    @Unique
    private static final net.minecraft.util.Identifier RESTRICT_TEXTURE = FSitMod.id("textures/gui/restrict_button.png");
    *//*?} else {*/
    @Unique
    private static final net.minecraft.client.gui.screen.ButtonTextures RESTRICT_TEXTURE = new net.minecraft.client.gui.screen.ButtonTextures(FSitMod.id("social_interactions/restrict_button"), FSitMod.id("social_interactions/restrict_button_disabled"), FSitMod.id("social_interactions/restrict_button_highlighted"));
    @Unique
    private static final net.minecraft.client.gui.screen.ButtonTextures ALLOW_TEXTURE = new net.minecraft.client.gui.screen.ButtonTextures(FSitMod.id("social_interactions/allow_button"), FSitMod.id("social_interactions/allow_button_disabled"), FSitMod.id("social_interactions/allow_button_highlighted"));
    /*?} */
    @Unique
    private static final Text RESTRICT_BUTTON = FSitMod.translatable("gui", "socialInteractions.restrict");
    @Unique
    private static final Text ALLOW_BUTTON = FSitMod.translatable("gui", "socialInteractions.allow");
    @Unique
    private static final Text DISABLED_BUTTON = FSitMod.translatable("gui", "socialInteractions.disabled");
    @Shadow
    private @Final List<ClickableWidget> buttons;
    @Shadow
    private @Final UUID uuid;
    @Unique
    private ButtonWidget restrictButton;
    @Unique
    private ButtonWidget allowButton;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/SocialInteractionsPlayerListEntry;setShowButtonVisible(Z)V"))
    protected void restrictButtons(CallbackInfo ci) {
        if (FSitModClient.isServerFSitCompatible()) {
            /*? if <=1.20.1 {*//*
            this.restrictButton = new TexturedButtonWidget(0, 0, 20, 20, 0, 0, 20, RESTRICT_TEXTURE, this::restrict);
            this.allowButton = new TexturedButtonWidget(0, 0, 20, 20, 20, 0, 20, RESTRICT_TEXTURE, this::allow);
            *//*?} else {*/
            this.restrictButton = new TexturedButtonWidget(20, 20, RESTRICT_TEXTURE, this::restrict, RESTRICT_BUTTON);
            this.allowButton = new TexturedButtonWidget(20, 20, ALLOW_TEXTURE, this::allow, ALLOW_BUTTON);
            /*?} */

            this.restrictButton.active = FSitMod.getConfig().getOnUse().getRiding();
            this.restrictButton.setTooltip(Tooltip.of(this.restrictButton.active ? RESTRICT_BUTTON : DISABLED_BUTTON));

            this.allowButton.active = FSitMod.getConfig().getOnUse().getRiding();
            this.allowButton.setTooltip(Tooltip.of(this.allowButton.active ? ALLOW_BUTTON : DISABLED_BUTTON));

            buttons.add(this.restrictButton);
            updateButtons(RestrictionList.isRestricted(uuid));
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void renderRestrictButtons(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        if (this.restrictButton != null && this.allowButton != null) {
            final int offset = 24 * (buttons.size() - 1);

            this.restrictButton.setX(x + (entryWidth - this.restrictButton.getWidth() - 4) - offset);
            this.restrictButton.setY(y + (entryHeight - this.restrictButton.getHeight()) / 2);
            this.restrictButton.render(context, mouseX, mouseY, tickDelta);
            this.allowButton.setX(x + (entryWidth - this.allowButton.getWidth() - 4) - offset);
            this.allowButton.setY(y + (entryHeight - this.allowButton.getHeight()) / 2);
            this.allowButton.render(context, mouseX, mouseY, tickDelta);
        }
    }

    @Unique
    private void updateButtons(boolean isRestricted) {
        this.restrictButton.visible = !isRestricted;
        this.allowButton.visible = isRestricted;
    }

    @Unique
    private void restrict(ButtonWidget button) {
        RestrictionList.add(uuid);
        this.updateButtons(true);
    }

    @Unique
    private void allow(ButtonWidget button) {
        RestrictionList.remove(uuid);
        this.updateButtons(false);
    }
}
