package dev.rvbsm.fsit.config;

import dev.rvbsm.fsit.config.option.BlockSetOption;
import dev.rvbsm.fsit.config.option.Option;
import dev.rvbsm.fsit.config.option.TagKeyBlockSetOption;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public abstract class FSitConfig {
	public static final Option<Boolean> sneakSit = new Option<>("sneak", "sneak_sit", true);
	public static final Option<Double> minAngle = new Option<>("sneak", "min_angle", 66.0D);
	public static final Option<Integer> shiftDelay = new Option<>("sneak", "shift_delay", 600);
	public static final BlockSetOption sittableBlocks = new BlockSetOption("sittable_blocks", List.of());
	public static final TagKeyBlockSetOption sittableTags = new TagKeyBlockSetOption("sittable_tags", List.of("minecraft:slabs", "minecraft:stairs", "minecraft:logs"));
	public static final Option<Boolean> sitOnPlayers = new Option<>("misc", "sit_on_players", false);

	@SuppressWarnings({"unchecked", "rawtypes"})
	protected static void load() {
		for (Field field : FSitConfig.class.getDeclaredFields())
			if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()) && Option.class.isAssignableFrom(field.getType())) {
				try {
					final Option option = (Option) field.get(null);
					final Object value = FSitConfigManager.config.getOrElse(option.getPath(), option.getDefaultValue());
					option.setValue(value);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		FSitConfigManager.save(); // for empty fields
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	protected static void save() {
		for (Field field : FSitConfig.class.getDeclaredFields())
			if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()) && Option.class.isAssignableFrom(field.getType())) {
				try {
					final Option option = (Option) field.get(null);
					FSitConfigManager.config.set(option.getPath(), option.getValue());
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
	}
}
