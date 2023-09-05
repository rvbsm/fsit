package dev.rvbsm.fsit.config;

import java.util.Map;

final class ConfigMigrator {

	private static final Map<String, String> migratable = Map.of(
					"sneak_sit", ConfigData.Fields.SNEAK_ENABLED,
					"sneak.sneak_delay", ConfigData.Fields.SNEAK_DELAY,
					"misc.sit_players", ConfigData.Fields.RIDE_ENABLED,
					"sneak.min_angle", ConfigData.Fields.SNEAK_ANGLE
	);

	private ConfigMigrator() {}

	static void tryMigrate() {
		FSitConfig.config.set("config_version", ConfigData.Entries.CONFIG_VERSION.defaultValue());

		for (var entry : migratable.entrySet())
			if (FSitConfig.config.contains(entry.getKey())) migrate(entry.getKey(), entry.getValue());
	}

	private static <T> void migrate(String from, String to) {
		final T value = FSitConfig.config.remove(from);
		FSitConfig.config.removeComment(from);
		FSitConfig.config.set(to, value);
	}
}
