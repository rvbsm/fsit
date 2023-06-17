package dev.rvbsm.fsit.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.conversion.*;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.config.conversion.Comment;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;
import java.util.List;

public class ConfigData {

	@Path(Fields.CONFIG_VERSION)
	@Comment(Comments.CONFIG_VERSION)
	public int configVersion = Entries.CONFIG_VERSION.defaultValue;

	@Path(Fields.SNEAK_SIT)
	@Comment(Comments.SNEAK_SIT)
	public boolean sneakSit = Entries.SNEAK_SIT.defaultValue;

	@Path(Fields.MIN_ANGLE)
	@Comment(Comments.MIN_ANGLE)
	@SpecDoubleInRange(min = -90, max = 90)
	public double minAngle = Entries.MIN_ANGLE.defaultValue;

	@Path(Fields.SNEAK_DELAY)
	@Comment(Comments.SNEAK_DELAY)
	@SpecIntInRange(min = 100, max = 2000)
	public int sneakDelay = Entries.SNEAK_DELAY.defaultValue;

	@Path(Fields.SITTABLE_SIT)
	@Comment(Comments.SITTABLE_SIT)
	public boolean sittableSit = Entries.SITTABLE_SIT.defaultValue;

	@Path(Fields.SITTABLE_BLOCKS)
	@Comment(Comments.SITTABLE_BLOCKS)
	@Conversion(Identifier2StringConverter.class)
	public List<Identifier> sittableBlocks = Entries.SITTABLE_BLOCKS.defaultValue.stream().map(Identifier::new).toList();

	@Path(Fields.SITTABLE_TAGS)
	@Comment(Comments.SITTABLE_TAGS)
	@Conversion(Identifier2StringConverter.class)
	public List<Identifier> sittableTags = Entries.SITTABLE_TAGS.defaultValue.stream().map(Identifier::new).toList();

	@Path(Fields.SIT_PLAYERS)
	@Comment(Comments.SIT_PLAYERS)
	public boolean sitPlayers = Entries.SIT_PLAYERS.defaultValue;

	public static UnmodifiableCommentedConfig defaultConfig() {
		final CommentedConfig defaultConfig = new ObjectConverter().toConfig(new ConfigData(), CommentedConfig::inMemory);

		for (Field field : ConfigData.class.getDeclaredFields())
			if (field.isAnnotationPresent(Path.class) && field.isAnnotationPresent(Comment.class))
				defaultConfig.setComment(field.getAnnotation(Path.class).value(), field.getAnnotation(Comment.class).value());

		return defaultConfig.unmodifiable();
	}

	public interface Fields {
		String CONFIG_VERSION = "config_version";
		String SNEAK_SIT = "sneak.sneak_sit";
		String MIN_ANGLE = "sneak.min_angle";
		String SNEAK_DELAY = "sneak.sneak_delay";
		String SITTABLE_SIT = "sittable.sit";
		String SITTABLE_BLOCKS = "sittable.blocks";
		String SITTABLE_TAGS = "sittable.tags";
		String SIT_PLAYERS = "misc.sit_players";
	}

	private interface Comments {
		String CONFIG_VERSION = "Do not edit";
		String SNEAK_SIT = "Toggles sit-on-sneak feature";
		String MIN_ANGLE = "Minimal pitch to sitting down";
		String SNEAK_DELAY = "Time in ms between sneaks for sitting down";
		String SITTABLE_SIT = "Toggles sitting on specified blocks";
		String SITTABLE_BLOCKS = "List of block ids (e.g. \"oak_log\") available to sit";
		String SITTABLE_TAGS = "List of block tags";
		String SIT_PLAYERS = "Toggles sitting on other players";
	}

	public interface Entries {
		ConfigEntry<Integer> CONFIG_VERSION = new ConfigEntry<>(Fields.CONFIG_VERSION, 3);
		ConfigEntry<Boolean> SNEAK_SIT = new ConfigEntry<>(Fields.SNEAK_SIT, true);
		ConfigEntry<Double> MIN_ANGLE = new ConfigEntry<>(Fields.MIN_ANGLE, 66d);
		ConfigEntry<Integer> SNEAK_DELAY = new ConfigEntry<>(Fields.SNEAK_DELAY, 600);
		ConfigEntry<Boolean> SITTABLE_SIT = new ConfigEntry<>(Fields.SITTABLE_SIT, true);
		ConfigEntry<List<String>> SITTABLE_BLOCKS = new ConfigEntry<>(Fields.SITTABLE_BLOCKS, List.of());
		ConfigEntry<List<String>> SITTABLE_TAGS = new ConfigEntry<>(Fields.SITTABLE_TAGS, List.of("minecraft:slabs", "minecraft:stairs", "minecraft:logs"));
		ConfigEntry<Boolean> SIT_PLAYERS = new ConfigEntry<>(Fields.SIT_PLAYERS, false);

		record ConfigEntry<T>(String key, T defaultValue) {
			public void save(T value) {
				FSitConfig.config.set(this.key, value);
			}

			public Text keyText() {
				return FSitMod.getTranslation("option", this.key);
			}

			public Text commentText() {
				return FSitMod.getTranslation("comment", this.key);
			}
		}
	}

	private static class Identifier2StringConverter implements Converter<List<Identifier>, List<String>> {

		@Override
		public List<Identifier> convertToField(List<String> value) {
			if (value == null) return List.of();
			return value.stream().filter(Identifier::isValid).distinct().map(Identifier::new).toList();
		}

		@Override
		public List<String> convertFromField(List<Identifier> value) {
			return value.stream().map(Identifier::toString).distinct().toList();
		}
	}
}
