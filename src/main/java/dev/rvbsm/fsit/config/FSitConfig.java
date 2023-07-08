package dev.rvbsm.fsit.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import dev.rvbsm.fsit.config.conversion.Comment;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Field;

public abstract class FSitConfig {

	protected static final CommentedFileConfig config = FSitConfig.getConfig();

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
			if (field.isAnnotationPresent(Path.class) && field.isAnnotationPresent(Comment.class))
				config.setComment(field.getAnnotation(Path.class).value(), field.getAnnotation(Comment.class).value());

		config.save();
	}

	public static void save(ConfigData loadDestination) {
		config.save();
		FSitConfig.load(loadDestination);
	}
}
