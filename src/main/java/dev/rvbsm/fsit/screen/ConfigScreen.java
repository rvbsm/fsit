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

	private static final FSitConfigManager configManager = FSitMod.getConfigManager();
	private final FSitConfig config;
	private final Screen previous;

	public ConfigScreen(Screen screen) {
		super(screen, MinecraftClient.getInstance().options, Text.translatable("fsit.options"));
		this.previous = screen;
		this.config = ConfigScreen.configManager.load();
	}

	@Override protected void init() {
		final GridWidget gridWidget = new GridWidget();
		final GridWidget.Adder adder = gridWidget.createAdder(2);
		gridWidget.getMainPositioner().marginX(5).marginBottom(4).alignHorizontalCenter();

		final Integer minAngle = this.config.minAngle.getValue().intValue();
		final Integer shiftDelay = this.config.shiftDelay.getValue().intValue();
		final Set<String> sittableBlocks = this.config.sittableBlocks.getValue();
		final Set<String> sittableTags = this.config.sittableTags.getValue();

		final Consumer<Integer> setMinAngle = value -> this.config.minAngle.setValue(value.doubleValue());
		final Consumer<Integer> setShiftDelay = value -> this.config.shiftDelay.setValue(value.longValue());

		final SimpleOption<Integer> minAngleOption = new SimpleOption<>(this.config.minAngle.getTranslationKey(), SimpleOption.emptyTooltip(), (prefix, value) -> GameOptions.getGenericValueText(prefix, Text.of(value + "°")), new SimpleOption.ValidatingIntSliderCallbacks(-90, 90), minAngle, setMinAngle);
		final SimpleOption<Integer> shiftDelayOption = new SimpleOption<>(this.config.shiftDelay.getTranslationKey(), SimpleOption.emptyTooltip(), (prefix, value) -> GameOptions.getGenericValueText(prefix, Text.of(value + "ms")), new SimpleOption.ValidatingIntSliderCallbacks(100, 2000), shiftDelay, setShiftDelay);

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
		configManager.save(this.config);
	}

	@Override public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
	}
}
