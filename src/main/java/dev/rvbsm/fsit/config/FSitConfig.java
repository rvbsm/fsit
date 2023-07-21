package dev.rvbsm.fsit.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import dev.rvbsm.fsit.config.annotation.Comment;
import dev.rvbsm.fsit.config.annotation.Environment;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Field;

public final class FSitConfig {

	static final CommentedFileConfig config = FSitConfig.getConfig();

	private FSitConfig() {}

	private static CommentedFileConfig getConfig() {
		final java.nio.file.Path configPath = FabricLoader.getInstance().getConfigDir().resolve("fsit.toml");

		Config.setInsertionOrderPreserved(true);
		return CommentedFileConfig.of(configPath, TomlFormat.instance());
	}

	public static void load(ConfigData destination) {
		config.load();
		if (config.contains("config_version") && !config.isNull("config_version") && config.get("config_version").equals(2)) ConfigMigrator.migrateFrom2();

		new ObjectConverter().toObject(config, destination);
		config.clear();
		new ObjectConverter().toConfig(destination, config);
		for (Field field : destination.getClass().getDeclaredFields())
			if (field.isAnnotationPresent(Path.class)) {
				final String path = field.getAnnotation(Path.class).value();
				if (field.isAnnotationPresent(Comment.class))
					config.setComment(path, field.getAnnotation(Comment.class).value());
				if (field.isAnnotationPresent(Environment.class) && field.getAnnotation(Environment.class).value() != FabricLoader.getInstance().getEnvironmentType())
					config.remove(path);
			}

		config.save();
	}

	public static void save(ConfigData loadDestination) {
		config.save();
		FSitConfig.load(loadDestination);
	}
}
