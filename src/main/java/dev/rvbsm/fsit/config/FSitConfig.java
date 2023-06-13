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

		defaultConfig.set(FSitConfigEntry.Fields.CONFIG_VERSION, FSitConfigEntry.CONFIG_VERSION.defaultValue());
		defaultConfig.setComment(FSitConfigEntry.Fields.CONFIG_VERSION, FSitConfigEntry.CONFIG_VERSION.comment());

		defaultConfig.set(FSitConfigEntry.Fields.SNEAK_SIT, FSitConfigEntry.SNEAK_SIT.defaultValue());
		defaultConfig.setComment(FSitConfigEntry.Fields.SNEAK_SIT, FSitConfigEntry.SNEAK_SIT.comment());

		defaultConfig.set(FSitConfigEntry.Fields.MIN_ANGLE, FSitConfigEntry.MIN_ANGLE.defaultValue());
		defaultConfig.setComment(FSitConfigEntry.Fields.MIN_ANGLE, FSitConfigEntry.MIN_ANGLE.comment());

		defaultConfig.set(FSitConfigEntry.Fields.SNEAK_DELAY, FSitConfigEntry.SNEAK_DELAY.defaultValue());
		defaultConfig.setComment(FSitConfigEntry.Fields.SNEAK_DELAY, FSitConfigEntry.SNEAK_DELAY.comment());

		defaultConfig.set(FSitConfigEntry.Fields.SITTABLE_BLOCKS, FSitConfigEntry.SITTABLE_BLOCKS.defaultValue());
		defaultConfig.setComment(FSitConfigEntry.Fields.SITTABLE_BLOCKS, FSitConfigEntry.SITTABLE_BLOCKS.comment());

		defaultConfig.set(FSitConfigEntry.Fields.SITTABLE_TAGS, FSitConfigEntry.SITTABLE_TAGS.defaultValue());
		defaultConfig.setComment(FSitConfigEntry.Fields.SITTABLE_TAGS, FSitConfigEntry.SITTABLE_TAGS.comment());

		defaultConfig.set(FSitConfigEntry.Fields.SIT_PLAYERS, FSitConfigEntry.SIT_PLAYERS.defaultValue());
		defaultConfig.setComment(FSitConfigEntry.Fields.SIT_PLAYERS, FSitConfigEntry.SIT_PLAYERS.comment());

		return defaultConfig.unmodifiable();
	}

	public static void load() {
		config.load();

		if (config.get(FSitConfigEntry.Fields.CONFIG_VERSION) != FSitConfigEntry.CONFIG_VERSION.defaultValue()) {
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
		@Path(FSitConfigEntry.Fields.CONFIG_VERSION)
		public int configVersion;

		@Path(FSitConfigEntry.Fields.SNEAK_SIT)
		public boolean sneakSit;

		@Path(FSitConfigEntry.Fields.MIN_ANGLE)
		@SpecDoubleInRange(min = -90, max = 90)
		public double minAngle;

		@Path(FSitConfigEntry.Fields.SNEAK_DELAY)
		@SpecIntInRange(min = 100, max = 2000)
		public int sneakDelay;

		@Path(FSitConfigEntry.Fields.SITTABLE_BLOCKS)
		@Conversion(Identifier2StringConverter.class)
		public List<Identifier> sittableBlocks;

		@Path(FSitConfigEntry.Fields.SITTABLE_TAGS)
		@Conversion(Identifier2StringConverter.class)
		public List<Identifier> sittableTags;

		@Path(FSitConfigEntry.Fields.SIT_PLAYERS)
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
