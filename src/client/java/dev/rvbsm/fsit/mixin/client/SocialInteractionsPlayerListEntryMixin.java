package dev.rvbsm.fsit.mixin.client;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.entity.RestrictHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Mixin(SocialInteractionsPlayerListEntry.class)
public abstract class SocialInteractionsPlayerListEntryMixin {

	@Unique
	private static final ButtonTextures RESTRICT_BUTTON_TEXTURES = new ButtonTextures(Identifier.of(FSitMod.MOD_ID, "social_interactions/restrict_button"), Identifier.of(FSitMod.MOD_ID, "social_interactions/restrict_button_disabled"), Identifier.of(FSitMod.MOD_ID, "social_interactions/restrict_button_highlighted"));
	@Unique
	private static final ButtonTextures ALLOW_BUTTON_TEXTURES = new ButtonTextures(Identifier.of(FSitMod.MOD_ID, "social_interactions/allow_button"), Identifier.of(FSitMod.MOD_ID, "social_interactions/allow_button_disabled"), Identifier.of(FSitMod.MOD_ID, "social_interactions/allow_button_highlighted"));
	@Unique
	private static final Text RESTRICT_BUTTON_TEXT = FSitMod.getTranslation("gui", "socialInteractions.restrict");
	@Unique
	private static final Text ALLOW_BUTTON_TEXT = FSitMod.getTranslation("gui", "socialInteractions.allow");
	@Unique
	private static final Text DISABLED_BUTTON_TEXT = FSitMod.getTranslation("gui", "socialInteractions.disabled");

	@Shadow
	@Final
	private List<ClickableWidget> buttons;
	@Unique
	private ButtonWidget allowButton;
	@Unique
	private ButtonWidget restrictButton;
//

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/multiplayer/SocialInteractionsPlayerListEntry;setShowButtonVisible(Z)V"))
	public void init(MinecraftClient client, SocialInteractionsScreen parent, UUID uuid, String name, Supplier<Identifier> skinTexture, boolean reportable, CallbackInfo ci) {
		final RestrictHandler restrictHandler = (RestrictHandler) client.player;

		this.restrictButton = new TexturedButtonWidget(0, 0, 20, 20, RESTRICT_BUTTON_TEXTURES, button -> {
			restrictHandler.fsit$restrictPlayer(uuid);
			updateButtons(false);
		}, RESTRICT_BUTTON_TEXT);
		this.restrictButton.active = FSitMod.getConfig().getRiding().isEnabled();
		this.restrictButton.setTooltip(Tooltip.of(this.restrictButton.active ? RESTRICT_BUTTON_TEXT : DISABLED_BUTTON_TEXT));
		this.restrictButton.setTooltipDelay(10);

		this.allowButton = new TexturedButtonWidget(0, 0, 20, 20, ALLOW_BUTTON_TEXTURES, button -> {
			restrictHandler.fsit$allowPlayer(uuid);
			updateButtons(true);
		}, ALLOW_BUTTON_TEXT);
		this.allowButton.active = FSitMod.getConfig().getRiding().isEnabled();
		this.allowButton.setTooltip(Tooltip.of(this.allowButton.active ? ALLOW_BUTTON_TEXT : DISABLED_BUTTON_TEXT));
		this.allowButton.setTooltipDelay(10);

		this.buttons.add(this.restrictButton);
		this.buttons.add(this.allowButton);

		this.updateButtons(!restrictHandler.fsit$isRestricted(uuid));
	}

	@Inject(method = "render", at = @At("TAIL"))
	public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
		if (this.restrictButton != null && this.allowButton != null) {
			this.restrictButton.setX(x + (entryWidth - this.restrictButton.getWidth() - 4) - 24 * (this.buttons.size() - 2));
			this.restrictButton.setY(y + (entryHeight - this.restrictButton.getHeight()) / 2);
			this.restrictButton.render(context, mouseX, mouseY, tickDelta);
			this.allowButton.setX(x + (entryWidth - this.allowButton.getWidth() - 4) - 24 * (this.buttons.size() - 2));
			this.allowButton.setY(y + (entryHeight - this.allowButton.getHeight()) / 2);
			this.allowButton.render(context, mouseX, mouseY, tickDelta);
		}
	}

	@Unique
	private void updateButtons(boolean showRestrictButton) {
		this.restrictButton.visible = showRestrictButton;
		this.allowButton.visible = !showRestrictButton;
	}
}
