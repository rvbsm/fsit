package dev.rvbsm.fsit.config;

public abstract class ConfigMigrator {

	protected static void migrate2To3() {
		FSitConfig.config.set("config_version", 3);

		if (FSitConfig.config.contains("sneak_sit")) {
			final boolean sneak_sit = FSitConfig.config.remove("sneak_sit");
			FSitConfig.config.set(ConfigData.Fields.SNEAK_ENABLED, sneak_sit);
		}

		if (FSitConfig.config.contains("sneak.sneak_delay")) {
			final int sneak_delay = FSitConfig.config.remove("sneak.sneak_delay");
			FSitConfig.config.set(ConfigData.Fields.SNEAK_DELAY, sneak_delay);
		}

		if (FSitConfig.config.contains("misc.sit_players")) {
			final boolean ride_players = FSitConfig.config.remove("misc.sit_players");
			FSitConfig.config.set(ConfigData.Fields.RIDE_PLAYERS, ride_players);
		}
	}
}
