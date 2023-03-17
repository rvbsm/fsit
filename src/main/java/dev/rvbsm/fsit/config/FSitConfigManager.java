package dev.rvbsm.fsit.config;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import dev.rvbsm.fsit.FSitMod;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FSitConfigManager {

	private static final Path configDir = FabricLoader.getInstance().getConfigDir();
	private static final Path configPath = FSitConfigManager.getConfigPath();
	private static final TomlWriter writer = new TomlWriter();

	private static @NotNull Path getConfigPath() {
		return configDir.resolve(FSitMod.getModId() + ".toml");
	}

	public FSitConfig load() {
		if (Files.exists(configPath)) return new Toml().read(configPath.toFile()).to(FSitConfigSimple.class).cast();
		else return this.create();
	}

	private FSitConfig create() {
		try {
			writer.write(new FSitConfig().toSimple(), configPath.toFile());
			return this.load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void save(FSitConfig config) {
		if (Files.exists(configPath))
			try {
				writer.write(config.toSimple(), configPath.toFile());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		else this.create();
	}
}
