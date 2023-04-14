package dev.rvbsm.fsit;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.rvbsm.fsit.config.FSitConfig;
import dev.rvbsm.fsit.config.FSitConfigManager;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public class FSitModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> {
			final ConfigBuilder configBuilder = ConfigBuilder.create()
							.setParentScreen(screen)
							.setTitle(Text.literal("FSit"))
							.setSavingRunnable(FSitConfigManager::save);
			final ConfigEntryBuilder entryBuilder = configBuilder.entryBuilder();

			final Text sneakSitText = Text.translatable(FSitConfig.sneakSit.getTranslationKey());
			final boolean sneakSit = FSitConfig.sneakSit.getValue();
			final boolean sneakSitDefault = FSitConfig.sneakSit.getDefaultValue();

			final Text minAngleText = Text.translatable(FSitConfig.minAngle.getTranslationKey());
			final int minAngle = FSitConfig.minAngle.getValue().intValue();
			final int minAngleDefault = FSitConfig.minAngle.getDefaultValue().intValue();
			final Consumer<Integer> minAngleSave = value -> FSitConfig.minAngle.setValue(value.doubleValue());

			final Text shiftDelayText = Text.translatable(FSitConfig.shiftDelay.getTranslationKey());
			final int shiftDelay = FSitConfig.shiftDelay.getValue();
			final int shiftDelayDefault = FSitConfig.shiftDelay.getDefaultValue();

			final Text sittableBlocksText = Text.translatable(FSitConfig.sittableBlocks.getTranslationKey());
			final List<String> sittableBlocks = FSitConfig.sittableBlocks.getValue();
			final List<String> sittableBlocksDefault = FSitConfig.sittableBlocks.getDefaultValue();

			final Text sittableTagsText = Text.translatable(FSitConfig.sittableTags.getTranslationKey());
			final List<String> sittableTags = FSitConfig.sittableTags.getValue();
			final List<String> sittableTagsDefault = FSitConfig.sittableTags.getDefaultValue();


			final ConfigCategory main = configBuilder.getOrCreateCategory(Text.literal("Main"));
			main.addEntry(entryBuilder.startBooleanToggle(sneakSitText, sneakSit)
							.setDefaultValue(sneakSitDefault)
							.setSaveConsumer(FSitConfig.sneakSit::setValue)
							.build());
			main.addEntry(entryBuilder.startIntSlider(minAngleText, minAngle, -90, 90)
							.setDefaultValue(minAngleDefault)
							.setSaveConsumer(minAngleSave)
							.build());
			main.addEntry(entryBuilder.startIntSlider(shiftDelayText, shiftDelay, 100, 2000)
							.setDefaultValue(shiftDelayDefault)
							.setSaveConsumer(FSitConfig.shiftDelay::setValue)
							.build());
			main.addEntry(entryBuilder.startStrList(sittableBlocksText, sittableBlocks)
							.setDefaultValue(sittableBlocksDefault)
							.setSaveConsumer(FSitConfig.sittableBlocks::setValue)
							.build());
			main.addEntry(entryBuilder.startStrList(sittableTagsText, sittableTags)
							.setDefaultValue(sittableTagsDefault)
							.setSaveConsumer(FSitConfig.sittableTags::setValue)
							.build());

			return configBuilder.build();
		};
	}
}
