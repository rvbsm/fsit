package dev.rvbsm.fsit.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.conversion.*;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import dev.rvbsm.fsit.FSitMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.util.List;

public abstract class FSitConfig {

	protected static final CommentedFileConfig config = FSitConfig.getConfig();
	private static final UnmodifiableCommentedConfig defaultConfig = FSitConfig.getDefaultConfig();

	public static ConfigData data;

	private static CommentedFileConfig getConfig() {
		final java.nio.file.Path configPath = FabricLoader.getInstance().getConfigDir().resolve(FSitMod.getModId() + ".toml");

		return CommentedFileConfig.of(configPath, TomlFormat.instance());
	}

	private static UnmodifiableCommentedConfig getDefaultConfig() {
		final CommentedConfig defaultConfig = CommentedConfig.inMemory();

		defaultConfig.set(FSitConfigFields.CONFIG_VERSION, FSitConfigFields.ConfigEntry.CONFIG_VERSION.defaultValue());
		defaultConfig.setComment(FSitConfigFields.CONFIG_VERSION, FSitConfigFields.ConfigEntry.CONFIG_VERSION.comment());

		defaultConfig.set(FSitConfigFields.SNEAK_SIT, FSitConfigFields.ConfigEntry.SNEAK_SIT.defaultValue());
		defaultConfig.setComment(FSitConfigFields.SNEAK_SIT, FSitConfigFields.ConfigEntry.SNEAK_SIT.comment());

		defaultConfig.set(FSitConfigFields.MIN_ANGLE, FSitConfigFields.ConfigEntry.MIN_ANGLE.defaultValue());
		defaultConfig.setComment(FSitConfigFields.MIN_ANGLE, FSitConfigFields.ConfigEntry.MIN_ANGLE.comment());

		defaultConfig.set(FSitConfigFields.SNEAK_DELAY, FSitConfigFields.ConfigEntry.SNEAK_DELAY.defaultValue());
		defaultConfig.setComment(FSitConfigFields.SNEAK_DELAY, FSitConfigFields.ConfigEntry.SNEAK_DELAY.comment());

		defaultConfig.set(FSitConfigFields.SITTABLE_BLOCKS, FSitConfigFields.ConfigEntry.SITTABLE_BLOCKS.defaultValue());
		defaultConfig.setComment(FSitConfigFields.SITTABLE_BLOCKS, FSitConfigFields.ConfigEntry.SITTABLE_BLOCKS.comment());

		defaultConfig.set(FSitConfigFields.SITTABLE_TAGS, FSitConfigFields.ConfigEntry.SITTABLE_TAGS.defaultValue());
		defaultConfig.setComment(FSitConfigFields.SITTABLE_TAGS, FSitConfigFields.ConfigEntry.SITTABLE_TAGS.comment());

		defaultConfig.set(FSitConfigFields.SIT_PLAYERS, FSitConfigFields.ConfigEntry.SIT_PLAYERS.defaultValue());
		defaultConfig.setComment(FSitConfigFields.SIT_PLAYERS, FSitConfigFields.ConfigEntry.SIT_PLAYERS.comment());

		return defaultConfig.unmodifiable();
	}

	public static void load() {
		config.load();

		if (config.get(FSitConfigFields.CONFIG_VERSION) != FSitConfigFields.ConfigEntry.CONFIG_VERSION.defaultValue()) {
			config.clear();
			config.clearComments();
		}

		config.addAll(defaultConfig);
		config.putAllComments(defaultConfig);
		FSitConfig.save();

		FSitConfig.data = new ObjectConverter().toObject(config, ConfigData::new);
	}

	public static void save() {
		config.save();

		FSitConfig.data = new ObjectConverter().toObject(config, ConfigData::new);
	}

	public static class ConfigData {
		@Path(FSitConfigFields.CONFIG_VERSION)
		public int configVersion;

		@Path(FSitConfigFields.SNEAK_SIT)
		public boolean sneakSit;

		@Path(FSitConfigFields.MIN_ANGLE)
		@SpecDoubleInRange(min = -90, max = 90)
		public double minAngle;

		@Path(FSitConfigFields.SNEAK_DELAY)
		@SpecIntInRange(min = 100, max = 2000)
		public int sneakDelay;

		@Path(FSitConfigFields.SITTABLE_BLOCKS)
		@Conversion(Identifier2StringConverter.class)
		public List<Identifier> sittableBlocks;

		@Path(FSitConfigFields.SITTABLE_TAGS)
		@Conversion(Identifier2StringConverter.class)
		public List<Identifier> sittableTags;

		@Path(FSitConfigFields.SIT_PLAYERS)
		public boolean sitPlayers;

		private static class Identifier2StringConverter implements Converter<List<Identifier>, List<String>> {

			@Override
			public List<Identifier> convertToField(List<String> value) {
				return value.stream().map(Identifier::new).toList();
			}

			@Override
			public List<String> convertFromField(List<Identifier> value) {
				return value.stream().map(Identifier::toString).toList();
			}
		}
	}
}
