package dev.rvbsm.fsit.config;

import dev.rvbsm.fsit.config.option.BlockSetOption;
import dev.rvbsm.fsit.config.option.Option;
import dev.rvbsm.fsit.config.option.TagKeyBlockSetOption;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public abstract class FSitConfig {
	public static final Option<Boolean> sneakSit = new Option<>("sneak_sit", true);
	public static final Option<Double> minAngle = new Option<>("sneak.min_angle", 66.0D);
	public static final Option<Integer> sneakDelay = new Option<>("sneak.sneak_delay", 600);
	public static final BlockSetOption sittableBlocks = new BlockSetOption("sittable.blocks", List.of());
	public static final TagKeyBlockSetOption sittableTags = new TagKeyBlockSetOption("sittable.tags", List.of("minecraft:slabs", "minecraft:stairs", "minecraft:logs"));
	public static final Option<Boolean> sitPlayers = new Option<>("misc.sit_players", false);
	private static final Option<Integer> configVersion = new Option<>("config_version", 2);

	@SuppressWarnings({"unchecked", "rawtypes"})
	protected static void load() {
		final Integer version = FSitConfigManager.config.getOrElse(configVersion.getKey(), 0);
		if (!version.equals(configVersion.getValue())) {
			FSitConfigManager.config.clear();
			FSitConfigManager.config.set(FSitConfig.configVersion.getKey(), FSitConfig.configVersion.getDefaultValue());
		}

		for (Field field : FSitConfig.class.getDeclaredFields()) {
			if (!Option.class.isAssignableFrom(field.getType())) continue;

			final int modifiers = field.getModifiers();
			if (!Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers)) continue;

			try {
				final Option option = (Option) field.get(null);
				final Object value = FSitConfigManager.config.getOrElse(option.getKey(), option.getDefaultValue());

				if (Modifier.isPublic(modifiers)) option.setValue(value);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		FSitConfigManager.save(); // for empty fields
	}

	@SuppressWarnings("rawtypes")
	protected static void save() {
		for (Field field : FSitConfig.class.getDeclaredFields()) {
			if (!Option.class.isAssignableFrom(field.getType())) continue;

			final int modifiers = field.getModifiers();
			if (!Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers)) continue;

			try {
				final Option option = (Option) field.get(null);
				if (Modifier.isPublic(modifiers)) FSitConfigManager.config.set(option.getKey(), option.getValue());
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
}
