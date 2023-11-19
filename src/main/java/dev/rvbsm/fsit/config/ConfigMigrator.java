package dev.rvbsm.fsit.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ConfigMigrator {

	private final Map<String, String> migrationMap;

	ConfigMigrator(Map<String, String> migrationMap) {
		this.migrationMap = migrationMap;
	}

	private Optional<Object> getNestedValue(Map<String, Object> configMap, String nestedKey) {
		final String[] keys = nestedKey.split("\\.");
		for (int i = 0; i < keys.length; i++) {
			Object value = configMap.get(keys[i]);
			if (i < keys.length - 1) {
				if (value instanceof Map) configMap = (Map<String, Object>) value;
				else return Optional.empty();
			} else return Optional.ofNullable(value);
		}
		return Optional.empty();
	}

	private void putNestedValue(Map<String, Object> configMap, String nestedKey, Object value) {
		final String[] keys = nestedKey.split("\\.");
		for (int i = 0; i < keys.length; i++) {
			if (i < keys.length - 1) configMap = (Map<String, Object>) configMap.computeIfAbsent(keys[i], k -> new HashMap<>());
			else configMap.put(keys[i], value);
		}
	}

	private void removeNestedValue(Map<String, Object> configMap, String nestedKey) {
		final String[] keys = nestedKey.split("\\.");
		for (int i = 0; i < keys.length; i++) {
			if (i < keys.length - 1) {
				if (configMap.get(keys[i]) instanceof Map) configMap = (Map<String, Object>) configMap.get(keys[i]);
				else break;
			} else configMap.remove(keys[i]);
		}
	}

	void migrate(Map<String, Object> configMap) {
		this.migrationMap.forEach((oldFieldName, newFieldName) -> {
			final Optional<Object> oldFieldValue = this.getNestedValue(configMap, oldFieldName);
			if (oldFieldValue.isPresent()) {
				this.putNestedValue(configMap, newFieldName, oldFieldValue.get());
				this.removeNestedValue(configMap, oldFieldName);
			}
		});
		configMap.remove("config_version");
	}
}
