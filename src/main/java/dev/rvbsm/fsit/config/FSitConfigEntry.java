package dev.rvbsm.fsit.config;

import java.util.List;

public interface FSitConfigEntry {
	Entry<Integer> CONFIG_VERSION = new Entry<>(Fields.CONFIG_VERSION, 3, "Do not edit");
	Entry<Boolean> SNEAK_SIT = new Entry<>(Fields.SNEAK_SIT, true, "Toggles sit-on-sneak feature");
	Entry<Double> MIN_ANGLE = new Entry<>(Fields.MIN_ANGLE, 66d, "Minimal pitch to sitting down");
	Entry<Integer> SNEAK_DELAY = new Entry<>(Fields.SNEAK_DELAY, 600, "Time in ms between sneaks for sitting down");
	Entry<List<String>> SITTABLE_BLOCKS = new Entry<>(Fields.SITTABLE_BLOCKS, List.of(), "List of block ids (e.g. \"oak_log\") available to sit");
	Entry<List<String>> SITTABLE_TAGS = new Entry<>(Fields.SITTABLE_TAGS, List.of("minecraft:slabs", "minecraft:stairs", "minecraft:logs"), "List of block tags");
	Entry<Boolean> SIT_PLAYERS = new Entry<>(Fields.SIT_PLAYERS, false, "Toggles sitting on other players");

	interface Fields {
		String CONFIG_VERSION = "config_version";
		String SNEAK_SIT = "sneak.sneak_sit";
		String MIN_ANGLE = "sneak.min_angle";
		String SNEAK_DELAY = "sneak.sneak_delay";
		String SITTABLE_BLOCKS = "sittable.blocks";
		String SITTABLE_TAGS = "sittable.tags";
		String SIT_PLAYERS = "misc.sit_players";
	}

	record Entry<T>(String key, T defaultValue, String comment) {
		public void save(T value) {
			FSitConfig.config.set(this.key, value);
		}
	}
}
