package dev.rvbsm.fsit.config;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import dev.rvbsm.fsit.config.convertion.EnvStripLowerUnderscoreNamingStrategy;
import dev.rvbsm.fsit.config.convertion.EnvironmentExclusionStrategy;
import dev.rvbsm.fsit.config.convertion.IdentifierTypeAdapter;
import lombok.Getter;
import lombok.SneakyThrows;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager<T> {

	private static final Gson GSON = createGson();
	private static final Toml TOML_READER = new Toml();
	private static final TomlWriter TOML_WRITER = new TomlWriter();

	private final File configFile;
	private final Class<T> configClass;
	@Getter
	private T config;

	public ConfigManager(String configName, Class<T> configClass) {
		this.configFile = FabricLoader.getInstance().getConfigDir().resolve(configName + ".toml").toFile();
		this.configClass = configClass;
	}

	private static Gson createGson() {
		return new GsonBuilder().setExclusionStrategies(new EnvironmentExclusionStrategy())
						.setFieldNamingStrategy(new EnvStripLowerUnderscoreNamingStrategy())
						.registerTypeAdapter(Identifier.class, new IdentifierTypeAdapter())
						.setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
						.create();
	}

	public String stringify(T configData) {
		return GSON.toJson(configData);
	}

	public T configify(String configJson) {
		return GSON.fromJson(configJson, this.configClass);
	}

	public void loadConfig() {
		final Map<String, Object> configMap = this.configFile.exists() ? TOML_READER.read(this.configFile).toMap() : new HashMap<>();
		final String configJson = GSON.toJson(configMap);
		this.config = this.configify(configJson);

		this.saveConfig();
	}

	@SneakyThrows
	public void saveConfig() {
		final String configJson = this.stringify(this.config);
		final Map<String, Object> configMap = GSON.fromJson(configJson, new TypeToken<Map<String, Object>>() {}.getType());
		TOML_WRITER.write(configMap, this.configFile);
	}
}
