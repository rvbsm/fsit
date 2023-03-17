package dev.rvbsm.fsit.screen;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.config.FSitConfig;
import dev.rvbsm.fsit.config.FSitConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.Set;
import java.util.function.Consumer;

public class ConfigScreen extends GameOptionsScreen {

	private final Screen previous;

	public ConfigScreen(Screen screen) {
		super(screen, MinecraftClient.getInstance().options, Text.translatable("fsit.options"));
		this.previous = screen;
	}

	@Override protected void init() {
		final GridWidget gridWidget = new GridWidget();
		final GridWidget.Adder adder = gridWidget.createAdder(2);
		gridWidget.getMainPositioner().marginX(5).marginBottom(4).alignHorizontalCenter();

		final Integer minAngle = FSitConfig.minAngle.getValue().intValue();
		final Integer shiftDelay = FSitConfig.shiftDelay.getValue().intValue();
		final Set<String> sittableBlocks = FSitConfig.sittableBlocks.getValue();
		final Set<String> sittableTags = FSitConfig.sittableTags.getValue();

		final Consumer<Integer> setMinAngle = value -> FSitConfig.minAngle.setValue(value.doubleValue());
		final Consumer<Integer> setShiftDelay = value -> FSitConfig.shiftDelay.setValue(value.longValue());

		final SimpleOption<Integer> minAngleOption = new SimpleOption<>(FSitConfig.minAngle.getTranslationKey(), SimpleOption.emptyTooltip(), (prefix, value) -> GameOptions.getGenericValueText(prefix, Text.of(value + "°")), new SimpleOption.ValidatingIntSliderCallbacks(-90, 90), minAngle, setMinAngle);
		final SimpleOption<Integer> shiftDelayOption = new SimpleOption<>(FSitConfig.shiftDelay.getTranslationKey(), SimpleOption.emptyTooltip(), (prefix, value) -> GameOptions.getGenericValueText(prefix, Text.of(value + "ms")), new SimpleOption.ValidatingIntSliderCallbacks(100, 2000), shiftDelay, setShiftDelay);

		adder.add(minAngleOption.createWidget(this.gameOptions, 0, 0, 150));
		adder.add(shiftDelayOption.createWidget(this.gameOptions, 0, 0, 150));
		adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> this.client.setScreen(this.previous))
						.width(200)
						.build(), 2, adder.copyPositioner().marginTop(6));

		gridWidget.refreshPositions();
		SimplePositioningWidget.setPos(gridWidget, 0, this.height / 6 - 12, this.width, this.height, .5f, 0f);
		gridWidget.forEachChild(this::addDrawableChild);
	}

	@Override public void removed() {
		FSitConfigManager.save();
	}

	@Override public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
	}
}
