package dev.rvbsm.fsit.config;

import dev.rvbsm.fsit.config.option.BlockSetOption;
import dev.rvbsm.fsit.config.option.Option;
import dev.rvbsm.fsit.config.option.TagKeyBlockSetOption;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public class FSitConfig {
	public static final Option<Double> minAngle = new Option<>("min_angle", 66.0D);
	public static final Option<Integer> shiftDelay = new Option<>("shift_delay", 600);
	public static final Option<Boolean> sitOnPlayers = new Option<>("sit_on_players", false);
	public static final BlockSetOption sittableBlocks = new BlockSetOption("sittable_blocks", List.of());
	public static final TagKeyBlockSetOption sittableTags = new TagKeyBlockSetOption("sittable_tags", List.of("minecraft:slabs", "minecraft:stairs", "minecraft:logs"));

	@SuppressWarnings("unchecked")
	protected static void load() {
		for (Field field : FSitConfig.class.getDeclaredFields())
			if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()) && Option.class.isAssignableFrom(field.getType())) {
				try {
					final Option option = (Option) field.get(null);
					final Object value = FSitConfigManager.config.getOrElse(option.getKey(), option.getDefaultValue());
					option.setValue(value);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		FSitConfigManager.save(); // for empty fields
	}

	protected static void save() {
		for (Field field : FSitConfig.class.getDeclaredFields())
			if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()) && Option.class.isAssignableFrom(field.getType())) {
				try {
					final Option option = (Option) field.get(null);
					FSitConfigManager.config.set(option.getKey(), option.getDefaultValue());
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
	}
}
