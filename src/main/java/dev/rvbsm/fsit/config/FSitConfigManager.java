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

	public static void load() {
		if (Files.exists(configPath)) new Toml().read(configPath.toFile()).to(FSitConfigPrimitive.class).fromPrimitive();
		else FSitConfigManager.create();
	}

	private static void create() {
		try {
			writer.write(FSitConfig.toPrimitive(), configPath.toFile());
			FSitConfigManager.load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void save() {
		if (Files.exists(configPath))
			try {
				writer.write(FSitConfig.toPrimitive(), configPath.toFile());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		else FSitConfigManager.create();
	}
}
