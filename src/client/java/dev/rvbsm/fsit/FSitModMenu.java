package dev.rvbsm.fsit;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.config.FSitConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class FSitModMenu implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> {
			final ConfigBuilder configBuilder = ConfigBuilder.create()
							.setParentScreen(screen)
							.setTitle(Text.literal("FSit"))
							.setDefaultBackgroundTexture(new Identifier("minecraft:textures/block/deepslate_bricks.png"))
							.setSavingRunnable(FSitConfig::save);
			final ConfigEntryBuilder entryBuilder = configBuilder.entryBuilder();

			final List<String> sittableTags = FSitMod.config.sittableTags.stream().map(Identifier::toString).toList();
			final List<String> sittableBlocks = FSitMod.config.sittableBlocks.stream().map(Identifier::toString).toList();

			final ConfigCategory main = configBuilder.getOrCreateCategory(Text.literal("main"));

			final SubCategoryBuilder sneakCategory = entryBuilder.startSubCategory(FSitMod.getTranslation("category", "sneak"));
			sneakCategory.add(entryBuilder.startBooleanToggle(ConfigData.Entry.SNEAK_SIT.keyAsText(), FSitMod.config.sneakSit)
							.setDefaultValue(ConfigData.Entry.SNEAK_SIT::defaultValue)
							.setSaveConsumer(ConfigData.Entry.SNEAK_SIT::save)
							.setTooltip(ConfigData.Entry.SNEAK_SIT.commentAsText())
							.build());
			sneakCategory.add(entryBuilder.startIntSlider(ConfigData.Entry.MIN_ANGLE.keyAsText(), (int) FSitMod.config.minAngle, -90, 90)
							.setDefaultValue(ConfigData.Entry.MIN_ANGLE.defaultValue()::intValue)
							.setSaveConsumer((value) -> ConfigData.Entry.MIN_ANGLE.save(value.doubleValue()))
							.setTooltip(ConfigData.Entry.MIN_ANGLE.commentAsText())
							.build());
			sneakCategory.add(entryBuilder.startIntSlider(ConfigData.Entry.SNEAK_SIT.keyAsText(), FSitMod.config.sneakDelay, 100, 2000)
							.setDefaultValue(ConfigData.Entry.SNEAK_DELAY::defaultValue)
							.setSaveConsumer(ConfigData.Entry.SNEAK_DELAY::save)
							.setTooltip(ConfigData.Entry.SNEAK_DELAY.commentAsText())
							.build());

			final SubCategoryBuilder sittableCategory = entryBuilder.startSubCategory(FSitMod.getTranslation("category", "sittable"));
			sittableCategory.add(entryBuilder.startStrList(ConfigData.Entry.SITTABLE_TAGS.keyAsText(), sittableTags)
							.setDefaultValue(ConfigData.Entry.SITTABLE_TAGS::defaultValue)
							.setSaveConsumer(ConfigData.Entry.SITTABLE_TAGS::save)
							.setTooltip(ConfigData.Entry.SITTABLE_TAGS.commentAsText())
							.build());
			sittableCategory.add(entryBuilder.startStrList(ConfigData.Entry.SITTABLE_BLOCKS.keyAsText(), sittableBlocks)
							.setDefaultValue(ConfigData.Entry.SITTABLE_BLOCKS::defaultValue)
							.setSaveConsumer(ConfigData.Entry.SITTABLE_BLOCKS::save)
							.setTooltip(ConfigData.Entry.SITTABLE_BLOCKS.commentAsText())
							.build());

			final SubCategoryBuilder miscCategory = entryBuilder.startSubCategory(FSitMod.getTranslation("category", "misc"));
			miscCategory.add(entryBuilder.startBooleanToggle(ConfigData.Entry.SIT_PLAYERS.keyAsText(), FSitMod.config.sitPlayers)
							.setDefaultValue(ConfigData.Entry.SIT_PLAYERS::defaultValue)
							.setSaveConsumer(ConfigData.Entry.SIT_PLAYERS::save)
							.setTooltip(ConfigData.Entry.SIT_PLAYERS.commentAsText())
							.build());

			main.addEntry(sneakCategory.setExpanded(true).build());
			main.addEntry(sittableCategory.setExpanded(true).build());
			main.addEntry(miscCategory.setExpanded(true).build());

			return configBuilder.build();
		};
	}
}
