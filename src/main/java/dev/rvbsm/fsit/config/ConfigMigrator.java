package dev.rvbsm.fsit.config;

import com.electronwill.nightconfig.core.Config;

public abstract class ConfigMigrator {

	protected static void migrate2To3(Config config) {
		config.set("config_version", 3);

		if (config.contains("sneak_sit")) {
			final boolean sneak_sit = config.remove("sneak_sit");
			config.set("sneak.sit", sneak_sit);
		}

		if (config.contains("sneak.sneak_delay")) {
			final int sneak_delay = config.remove("sneak.sneak_delay");
			config.set("sneak.delay", sneak_delay);
		}

		if (config.contains("misc.sit_players")) {
			final boolean ride_players = config.remove("misc.sit_players");
			config.set("misc.ride_players", ride_players);
		}
	}
}
