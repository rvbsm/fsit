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
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigManager<T> {

	private static final Gson GSON = createGson();
	private static final Toml TOML_READER = new Toml();
	private static final TomlWriter TOML_WRITER = new TomlWriter();

	private final File configFile;
	private final Class<T> configClass;
	private final ConfigMigrator configMigrator;

	@Getter
	private T config;
	private Map<String, Object> configMap = new LinkedHashMap<>();

	public ConfigManager(String configName, Class<T> configClass, Map<String, String> migrationMap) {
		this.configFile = getConfigFile(configName);
		this.configClass = configClass;
		this.configMigrator = new ConfigMigrator(migrationMap);
	}

	private static Gson createGson() {
		return new GsonBuilder().setExclusionStrategies(new EnvironmentExclusionStrategy())
						.setFieldNamingStrategy(new EnvStripLowerUnderscoreNamingStrategy())
						.registerTypeAdapter(Identifier.class, new IdentifierTypeAdapter())
						.setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
						.create();
	}

	private static File getConfigFile(String configName) {
		return FabricLoader.getInstance().getConfigDir().resolve(configName + ".toml").toFile();
	}

	private static Map<String, Object> flatMap(Map<String, Object> fromMap, String prefix) {
		final Map<String, Object> toMap = new LinkedHashMap<>();
		fromMap.forEach((key, value) -> {
			final String toKey = prefix.isEmpty() ? key : (prefix + "." + key);
			if (value instanceof Map<?, ?>) toMap.putAll(flatMap((Map<String, Object>) value, toKey));
			else toMap.put(toKey, value);
		});

		return toMap;
	}

	private static Map<String, Object> nestedMap(Map<String, Object> fromMap) {
		final Map<String, Object> toMap = new LinkedHashMap<>();
		fromMap.forEach((fromKey, value) -> {
			String[] keys = fromKey.split("\\.");
			Map<String, Object> subMap = toMap;
			for (int i = 0; i < keys.length - 1; i++)
				subMap = (Map<String, Object>) subMap.computeIfAbsent(keys[i], k -> new LinkedHashMap<>());
			subMap.put(keys[keys.length - 1], value);
		});

		return toMap;
	}

	private void load(Map<String, Object> configMap) {
		final String configJson = GSON.toJson(configMap);
		this.config = this.configify(configJson);
	}

	public String stringify(T configData) {
		return GSON.toJson(configData);
	}

	public String tomlify() {
		return TOML_WRITER.write(this.mapify(this.config));
	}

	public Map<String, Object> mapify(T configData) {
		return GSON.fromJson(this.stringify(configData), new TypeToken<Map<String, Object>>() {}.getType());
	}

	public T configify(String configJson) {
		return GSON.fromJson(configJson, this.configClass);
	}

	public void loadConfig() {
		final Map<String, Object> configMap = this.configFile.exists()
						? TOML_READER.read(this.configFile).toMap()
						: new LinkedHashMap<>();
		this.configMigrator.migrate(configMap);

		this.load(configMap);
		this.saveConfig();
	}

	@SneakyThrows
	public void saveConfig() {
		final Map<String, Object> configMap = this.mapify(this.config);
		TOML_WRITER.write(configMap, this.configFile);

		this.configMap = flatMap(configMap, "");
	}

	public Object getByFlat(String key) {
		return this.configMap.get(key);
	}

	public void updateByFlat(String key, Object value) {
		this.configMap.put(key, value);
		final Map<String, Object> nestedMap = nestedMap(this.configMap);

		this.load(nestedMap);
		this.saveConfig();
	}
}
