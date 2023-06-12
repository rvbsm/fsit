package dev.rvbsm.fsit;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.rvbsm.fsit.config.FSitConfig;
import dev.rvbsm.fsit.config.FSitConfigFields;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class FSitModMenu implements ModMenuApi {

	private static final Text SNEAK_SIT_TEXT = FSitMod.getTranslation("option", FSitConfigFields.SNEAK_SIT);
	private static final Text MIN_ANGLE_TEXT = FSitMod.getTranslation("option", FSitConfigFields.MIN_ANGLE);
	private static final Text SNEAK_DELAY_TEXT = FSitMod.getTranslation("option", FSitConfigFields.SNEAK_DELAY);
	private static final Text SITTABLE_BLOCKS_TEXT = FSitMod.getTranslation("option", FSitConfigFields.SITTABLE_BLOCKS);
	private static final Text SITTABLE_TAGS_TEXT = FSitMod.getTranslation("option", FSitConfigFields.SITTABLE_TAGS);


	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> {
			final ConfigBuilder configBuilder = ConfigBuilder.create()
							.setParentScreen(screen)
							.setTitle(Text.literal("FSit"))
							.setSavingRunnable(FSitConfig::save);
			final ConfigEntryBuilder entryBuilder = configBuilder.entryBuilder();

			final List<String> sittableBlocks = FSitConfig.data.sittableBlocks.stream().map(Identifier::toString).toList();
			final List<String> sittableTags = FSitConfig.data.sittableTags.stream().map(Identifier::toString).toList();

			final ConfigCategory main = configBuilder.getOrCreateCategory(Text.literal("Main"));
			main.addEntry(entryBuilder.startBooleanToggle(SNEAK_SIT_TEXT, FSitConfig.data.sneakSit)
							.setDefaultValue(FSitConfigFields.ConfigEntry.SNEAK_SIT::defaultValue)
							.setSaveConsumer(FSitConfigFields.ConfigEntry.SNEAK_SIT::save)
							.build());
			main.addEntry(entryBuilder.startIntSlider(MIN_ANGLE_TEXT, (int) FSitConfig.data.minAngle, -90, 90)
							.setDefaultValue(FSitConfigFields.ConfigEntry.MIN_ANGLE.defaultValue()::intValue)
							.setSaveConsumer((value) -> FSitConfigFields.ConfigEntry.MIN_ANGLE.save(value.doubleValue()))
							.build());
			main.addEntry(entryBuilder.startIntSlider(SNEAK_DELAY_TEXT, FSitConfig.data.sneakDelay, 100, 2000)
							.setDefaultValue(FSitConfigFields.ConfigEntry.SNEAK_DELAY::defaultValue)
							.setSaveConsumer(FSitConfigFields.ConfigEntry.SNEAK_DELAY::save)
							.build());
			main.addEntry(entryBuilder.startStrList(SITTABLE_BLOCKS_TEXT, sittableBlocks)
							.setDefaultValue(FSitConfigFields.ConfigEntry.SITTABLE_BLOCKS::defaultValue)
							.setSaveConsumer(FSitConfigFields.ConfigEntry.SITTABLE_BLOCKS::save)
							.build());
			main.addEntry(entryBuilder.startStrList(SITTABLE_TAGS_TEXT, sittableTags)
							.setDefaultValue(FSitConfigFields.ConfigEntry.SITTABLE_TAGS::defaultValue)
							.setSaveConsumer(FSitConfigFields.ConfigEntry.SITTABLE_TAGS::save)
							.build());

			return configBuilder.build();
		};
	}
}
