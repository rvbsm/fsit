package dev.rvbsm.fsit.config;

import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import dev.rvbsm.fsit.FSitMod;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.function.Supplier;

public abstract class FSitConfig {

	protected static final CommentedFileConfig config = FSitConfig.getConfig(FSitMod.getModId());

	private static CommentedFileConfig getConfig(String configName) {
		final Path configPath = FabricLoader.getInstance().getConfigDir().resolve(configName + ".toml");

		return CommentedFileConfig.of(configPath, TomlFormat.instance());
	}

	public static <TConfig> TConfig load(Supplier<TConfig> destination, UnmodifiableCommentedConfig defaultConfig) {
		config.load();

		config.addAll(defaultConfig);
		config.putAllComments(defaultConfig);
		config.save();

		return new ObjectConverter().toObject(config, destination);
	}

	public static void save() {
		config.save();
		FSitMod.loadConfig();
	}
}
