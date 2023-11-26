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

public final class FSitModMenu implements ModMenuApi {

	private static final Text SNEAK_CATEGORY_TEXT = FSitMod.getTranslation("category", "sneak");
	private static final Text SITTABLE_CATEGORY_TEXT = FSitMod.getTranslation("category", "sittable");
	private static final Text RIDING_CATEGORY_TEXT = FSitMod.getTranslation("category", "riding");
	private static final Text SNEAK_ENABLED_TEXT = FSitMod.getTranslation("option", "sneak.enabled");
	private static final Text SNEAK_ENABLED_COMMENT = FSitMod.getTranslation("comment", "sneak.enabled");
	private static final Text SNEAK_ANGLE_TEXT = FSitMod.getTranslation("option", "sneak.angle");
	private static final Text SNEAK_ANGLE_COMMENT = FSitMod.getTranslation("comment", "sneak.angle");
	private static final Text SNEAK_DELAY_TEXT = FSitMod.getTranslation("option", "sneak.delay");
	private static final Text SNEAK_DELAY_COMMENT = FSitMod.getTranslation("comment", "sneak.delay");
	private static final Text SITTABLE_ENABLED_TEXT = FSitMod.getTranslation("option", "sittable.enabled");
	private static final Text SITTABLE_ENABLED_COMMENT = FSitMod.getTranslation("comment", "sittable.enabled");
	private static final Text SITTABLE_RADIUS_TEXT = FSitMod.getTranslation("option", "sittable.radius");
	private static final Text SITTABLE_RADIUS_COMMENT = FSitMod.getTranslation("comment", "sittable.radius");
	private static final Text SITTABLE_BLOCKS_TEXT = FSitMod.getTranslation("option", "sittable.blocks");
	private static final Text SITTABLE_BLOCKS_COMMENT = FSitMod.getTranslation("comment", "sittable.blocks");
	private static final Text SITTABLE_TAGS_TEXT = FSitMod.getTranslation("option", "sittable.tags");
	private static final Text SITTABLE_TAGS_COMMENT = FSitMod.getTranslation("comment", "sittable.tags");
	private static final Text RIDING_ENABLED_TEXT = FSitMod.getTranslation("option", "riding.enabled");
	private static final Text RIDING_ENABLED_COMMENT = FSitMod.getTranslation("comment", "riding.enabled");
	private static final Text RIDING_RADIUS_TEXT = FSitMod.getTranslation("option", "riding.radius");
	private static final Text RIDING_RADIUS_COMMENT = FSitMod.getTranslation("comment", "riding.radius");

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return screen -> {
			final ConfigBuilder configBuilder = ConfigBuilder.create()
							.setParentScreen(screen)
							.setTitle(Text.literal("FSit"))
							.setDefaultBackgroundTexture(new Identifier("minecraft:textures/block/deepslate_bricks.png"))
							.setSavingRunnable(FSitModClient::saveConfig);
			final ConfigEntryBuilder entryBuilder = configBuilder.entryBuilder();
			
			final ConfigData config = FSitMod.getConfig();
			final ConfigData.SneakTable configSneak = config.getSneak();
			final ConfigData.SittableTable configSittable = config.getSittable();
			final ConfigData.RidingTable configRiding = config.getRiding();

			final ConfigCategory main = configBuilder.getOrCreateCategory(Text.literal("main"));

			final SubCategoryBuilder sneakCategory = entryBuilder.startSubCategory(SNEAK_CATEGORY_TEXT);
			sneakCategory.add(entryBuilder.startBooleanToggle(SNEAK_ENABLED_TEXT, configSneak.isEnabled())
							.setSaveConsumer(configSneak::setEnabled)
							.setDefaultValue(ConfigData.DEFAULT.getSneak()::isEnabled)
							.setTooltip(SNEAK_ENABLED_COMMENT)
							.build());
			sneakCategory.add(entryBuilder.startIntSlider(SNEAK_ANGLE_TEXT, (int) configSneak.getAngle(), 0, 90)
							.setSaveConsumer(configSneak::setAngle)
							.setDefaultValue((int) ConfigData.DEFAULT.getSneak().getAngle())
							.setTooltip(SNEAK_ANGLE_COMMENT)
							.build());
			sneakCategory.add(entryBuilder.startIntSlider(SNEAK_DELAY_TEXT, configSneak.getDelay(), 100, 2000)
							.setSaveConsumer(configSneak::setDelay)
							.setDefaultValue(ConfigData.DEFAULT.getSneak()::getDelay)
							.setTooltip(SNEAK_DELAY_COMMENT)
							.build());

			final SubCategoryBuilder sittableCategory = entryBuilder.startSubCategory(SITTABLE_CATEGORY_TEXT);
			sittableCategory.add(entryBuilder.startBooleanToggle(SITTABLE_ENABLED_TEXT, configSittable.isEnabled())
							.setSaveConsumer(configSittable::setEnabled)
							.setDefaultValue(ConfigData.DEFAULT.getSittable()::isEnabled)
							.setTooltip(SITTABLE_ENABLED_COMMENT)
							.build());
			sittableCategory.add(entryBuilder.startIntSlider(SITTABLE_RADIUS_TEXT, configSittable.getRadius(), 0, 4)
							.setSaveConsumer(configSittable::setRadius)
							.setDefaultValue(ConfigData.DEFAULT.getSittable()::getRadius)
							.setTooltip(SITTABLE_RADIUS_COMMENT)
							.build());
			sittableCategory.add(entryBuilder.startStrList(SITTABLE_BLOCKS_TEXT, configSittable.getBlocksString())
							.setSaveConsumer(configSittable::setBlocksString)
							.setDefaultValue(ConfigData.DEFAULT.getSittable()::getBlocksString)
							.setTooltip(SITTABLE_BLOCKS_COMMENT)
							.build());
			sittableCategory.add(entryBuilder.startStrList(SITTABLE_TAGS_TEXT, configSittable.getTagsString())
							.setSaveConsumer(configSittable::setTagsString)
							.setDefaultValue(ConfigData.DEFAULT.getSittable()::getTagsString)
							.setTooltip(SITTABLE_TAGS_COMMENT)
							.build());

			final SubCategoryBuilder ridingCategory = entryBuilder.startSubCategory(RIDING_CATEGORY_TEXT);
			ridingCategory.add(entryBuilder.startBooleanToggle(RIDING_ENABLED_TEXT, configRiding.isEnabled())
							.setSaveConsumer(configRiding::setEnabled)
							.setDefaultValue(ConfigData.DEFAULT.getRiding()::isEnabled)
							.setTooltip(RIDING_ENABLED_COMMENT)
							.build());
			ridingCategory.add(entryBuilder.startIntSlider(RIDING_RADIUS_TEXT, configRiding.getRadius(), 0, 4)
							.setSaveConsumer(configRiding::setRadius)
							.setDefaultValue(ConfigData.DEFAULT.getRiding()::getRadius)
							.setTooltip(RIDING_RADIUS_COMMENT)
							.build());

			main.addEntry(sneakCategory.setExpanded(true).build());
			main.addEntry(sittableCategory.setExpanded(true).build());
			main.addEntry(ridingCategory.setExpanded(true).build());

			return configBuilder.build();
		};
	}
}
