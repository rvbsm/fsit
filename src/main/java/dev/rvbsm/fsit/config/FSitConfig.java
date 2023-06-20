package dev.rvbsm.fsit.config;

import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.config.conversion.Comment;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Field;
import java.nio.file.Path;

public abstract class FSitConfig {

	protected static final CommentedFileConfig config = FSitConfig.getConfig();

	private static CommentedFileConfig getConfig() {
		final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("fsit.toml");

		return CommentedFileConfig.of(configPath, TomlFormat.instance());
	}

	public static void load(ConfigData destination) {
		config.load();
		if (config.contains("config_version") && config.getInt("config_version") == 2) ConfigMigrator.migrate2To3(config);

		new ObjectConverter().toObject(config, destination);
		new ObjectConverter().toConfig(destination, config);
		for (Field field : destination.getClass().getDeclaredFields())
			if (field.isAnnotationPresent(com.electronwill.nightconfig.core.conversion.Path.class) && field.isAnnotationPresent(Comment.class))
				config.setComment(field.getAnnotation(com.electronwill.nightconfig.core.conversion.Path.class).value(), field.getAnnotation(Comment.class).value());

		config.save();
	}

	public static void save() {
		config.save();
		FSitMod.loadConfig();
	}
}
