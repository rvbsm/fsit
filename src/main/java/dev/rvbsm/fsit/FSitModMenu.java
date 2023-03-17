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
	@Override public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> {
			final ConfigBuilder configBuilder = ConfigBuilder.create()
							.setParentScreen(screen)
							.setTitle(Text.literal("FSit"))
							.setSavingRunnable(FSitConfigManager::save);
			final ConfigEntryBuilder entryBuilder = configBuilder.entryBuilder();

			final Text minAngleText = Text.translatable(FSitConfig.minAngle.getTranslationKey());
			final int minAngle = FSitConfig.minAngle.getValue().intValue();
			final Consumer<Integer> minAngleSave = value -> FSitConfig.minAngle.setValue(value.doubleValue());

			final Text shiftDelayText = Text.translatable(FSitConfig.shiftDelay.getTranslationKey());
			final int shiftDelay = FSitConfig.shiftDelay.getValue().intValue();
			final Consumer<Integer> shiftDelaySave = value -> FSitConfig.shiftDelay.setValue(value.longValue());

			final Text sittableBlocksText = Text.translatable(FSitConfig.sittableBlocks.getTranslationKey());
			final List<String> sittableBlocks = FSitConfig.sittableBlocks.getValue().stream().toList();

			final Text sittableTagsText = Text.translatable(FSitConfig.sittableTags.getTranslationKey());
			final List<String> sittableTags = FSitConfig.sittableTags.getValue().stream().toList();


			final ConfigCategory main = configBuilder.getOrCreateCategory(Text.literal("Main"));
			main.addEntry(entryBuilder.startIntSlider(minAngleText, minAngle, -90, 90)
							.setSaveConsumer(minAngleSave)
							.build());
			main.addEntry(entryBuilder.startIntSlider(shiftDelayText, shiftDelay, 100, 2000)
							.setSaveConsumer(shiftDelaySave)
							.build());
			main.addEntry(entryBuilder.startStrList(sittableBlocksText, sittableBlocks)
							.setSaveConsumer(FSitConfig.sittableBlocks::setValue)
							.build());
			main.addEntry(entryBuilder.startStrList(sittableTagsText, sittableTags)
							.setSaveConsumer(FSitConfig.sittableTags::setValue)
							.build());

			return configBuilder.build();
		};
	}
}
