package dev.rvbsm.fsit;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.rvbsm.fsit.config.ConfigData;
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
							.setSavingRunnable(FSitModClient::saveConfig);
			final ConfigEntryBuilder entryBuilder = configBuilder.entryBuilder();

			final List<String> sittableTags = FSitMod.config.sittableTags.stream().map(Identifier::toString).toList();
			final List<String> sittableBlocks = FSitMod.config.sittableBlocks.stream().map(Identifier::toString).toList();

			final ConfigCategory main = configBuilder.getOrCreateCategory(Text.literal("main"));

			final SubCategoryBuilder sneakCategory = entryBuilder.startSubCategory(FSitMod.getTranslation("category", "sneak"));
			sneakCategory.add(entryBuilder.startBooleanToggle(ConfigData.Entries.SNEAK_ENABLED.keyText(), FSitMod.config.sneak)
							.setDefaultValue(ConfigData.Entries.SNEAK_ENABLED::defaultValue)
							.setSaveConsumer(ConfigData.Entries.SNEAK_ENABLED::save)
							.setTooltip(ConfigData.Entries.SNEAK_ENABLED.commentText())
							.build());
			sneakCategory.add(entryBuilder.startIntSlider(ConfigData.Entries.MIN_ANGLE.keyText(), (int) FSitMod.config.minAngle, 0, 90)
							.setDefaultValue(ConfigData.Entries.MIN_ANGLE.defaultValue()::intValue)
							.setSaveConsumer((value) -> ConfigData.Entries.MIN_ANGLE.save(value.doubleValue()))
							.setTooltip(ConfigData.Entries.MIN_ANGLE.commentText())
							.build());
			sneakCategory.add(entryBuilder.startIntSlider(ConfigData.Entries.SNEAK_DELAY.keyText(), FSitMod.config.sneakDelay, 100, 2000)
							.setDefaultValue(ConfigData.Entries.SNEAK_DELAY::defaultValue)
							.setSaveConsumer(ConfigData.Entries.SNEAK_DELAY::save)
							.setTooltip(ConfigData.Entries.SNEAK_DELAY.commentText())
							.build());

			final SubCategoryBuilder sittableCategory = entryBuilder.startSubCategory(FSitMod.getTranslation("category", "sittable"));
			sittableCategory.add(entryBuilder.startBooleanToggle(ConfigData.Entries.SITTABLE_ENABLED.keyText(), FSitMod.config.sittable)
							.setDefaultValue(ConfigData.Entries.SITTABLE_ENABLED::defaultValue)
							.setSaveConsumer(ConfigData.Entries.SITTABLE_ENABLED::save)
							.setTooltip(ConfigData.Entries.SITTABLE_ENABLED.commentText())
							.build());
			sittableCategory.add(entryBuilder.startIntSlider(ConfigData.Entries.SITTABLE_RADIUS.keyText(), FSitModClient.config.sittableRadius, 0, 4)
							.setDefaultValue(ConfigData.Entries.SITTABLE_RADIUS::defaultValue)
							.setSaveConsumer(ConfigData.Entries.SITTABLE_RADIUS::save)
							.setTooltip(ConfigData.Entries.SITTABLE_RADIUS.commentText())
							.build());
			sittableCategory.add(entryBuilder.startStrList(ConfigData.Entries.SITTABLE_TAGS.keyText(), sittableTags)
							.setDefaultValue(ConfigData.Entries.SITTABLE_TAGS::defaultValue)
							.setSaveConsumer(ConfigData.Entries.SITTABLE_TAGS::save)
							.setTooltip(ConfigData.Entries.SITTABLE_TAGS.commentText())
							.build());
			sittableCategory.add(entryBuilder.startStrList(ConfigData.Entries.SITTABLE_BLOCKS.keyText(), sittableBlocks)
							.setDefaultValue(ConfigData.Entries.SITTABLE_BLOCKS::defaultValue)
							.setSaveConsumer(ConfigData.Entries.SITTABLE_BLOCKS::save)
							.setTooltip(ConfigData.Entries.SITTABLE_BLOCKS.commentText())
							.build());

			final SubCategoryBuilder miscCategory = entryBuilder.startSubCategory(FSitMod.getTranslation("category", "misc"));

			final SubCategoryBuilder rideCategory = entryBuilder.startSubCategory(FSitMod.getTranslation("category", "misc.riding"));
			rideCategory.add(entryBuilder.startBooleanToggle(ConfigData.Entries.RIDE_PLAYERS.keyText(), FSitMod.config.ridePlayers)
							.setDefaultValue(ConfigData.Entries.RIDE_PLAYERS::defaultValue)
							.setSaveConsumer(ConfigData.Entries.RIDE_PLAYERS::save)
							.setTooltip(ConfigData.Entries.RIDE_PLAYERS.commentText())
							.build());
			rideCategory.add(entryBuilder.startIntSlider(ConfigData.Entries.RIDE_RADIUS.keyText(), FSitModClient.config.rideRadius, 0, 4)
							.setDefaultValue(ConfigData.Entries.RIDE_RADIUS::defaultValue)
							.setSaveConsumer(ConfigData.Entries.RIDE_RADIUS::save)
							.setTooltip(ConfigData.Entries.RIDE_RADIUS.commentText())
							.build());
			rideCategory.add(entryBuilder.startDoubleField(ConfigData.Entries.RIDE_HEIGHT.keyText(), FSitMod.config.rideHeight)
							.setMin(0d)
							.setMax(1d)
							.setDefaultValue(ConfigData.Entries.RIDE_HEIGHT::defaultValue)
							.setSaveConsumer(ConfigData.Entries.RIDE_HEIGHT::save)
							.setTooltip(ConfigData.Entries.RIDE_HEIGHT.commentText())
							.build());

			miscCategory.add(rideCategory.build());

			main.addEntry(sneakCategory.setExpanded(true).build());
			main.addEntry(sittableCategory.setExpanded(true).build());
			main.addEntry(miscCategory.setExpanded(true).build());

			return configBuilder.build();
		};
	}
}
