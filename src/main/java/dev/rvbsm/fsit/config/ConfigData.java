package dev.rvbsm.fsit.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.conversion.*;
import dev.rvbsm.fsit.FSitMod;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class ConfigData {

	@Path(Fields.CONFIG_VERSION)
	public int configVersion = Entry.CONFIG_VERSION.defaultValue();
	@Path(Fields.SNEAK_SIT)
	public boolean sneakSit = Entry.SNEAK_SIT.defaultValue();
	@Path(Fields.MIN_ANGLE)
	@SpecDoubleInRange(min = -90, max = 90)
	public double minAngle = Entry.MIN_ANGLE.defaultValue();
	@Path(Fields.SNEAK_DELAY)
	@SpecIntInRange(min = 100, max = 2000)
	public int sneakDelay = Entry.SNEAK_DELAY.defaultValue();
	@Path(Fields.SITTABLE_BLOCKS)
	@Conversion(Identifier2StringConverter.class)
	public List<Identifier> sittableBlocks = Entry.SITTABLE_BLOCKS.defaultValue().stream().map(Identifier::new).toList();
	@Path(Fields.SITTABLE_TAGS)
	@Conversion(Identifier2StringConverter.class)
	public List<Identifier> sittableTags = Entry.SITTABLE_TAGS.defaultValue().stream().map(Identifier::new).toList();
	@Path(Fields.SIT_PLAYERS)
	public boolean sitPlayers = Entry.SIT_PLAYERS.defaultValue();

	public static UnmodifiableCommentedConfig getDefaultConfig() {
		final CommentedConfig defaultConfig = CommentedConfig.inMemory();

		defaultConfig.set(Fields.CONFIG_VERSION, Entry.CONFIG_VERSION.defaultValue());
		defaultConfig.setComment(Fields.CONFIG_VERSION, Entry.CONFIG_VERSION.comment());

		defaultConfig.set(Fields.SNEAK_SIT, Entry.SNEAK_SIT.defaultValue());
		defaultConfig.setComment(Fields.SNEAK_SIT, Entry.SNEAK_SIT.comment());

		defaultConfig.set(Fields.MIN_ANGLE, Entry.MIN_ANGLE.defaultValue());
		defaultConfig.setComment(Fields.MIN_ANGLE, Entry.MIN_ANGLE.comment());

		defaultConfig.set(Fields.SNEAK_DELAY, Entry.SNEAK_DELAY.defaultValue());
		defaultConfig.setComment(Fields.SNEAK_DELAY, Entry.SNEAK_DELAY.comment());

		defaultConfig.set(Fields.SITTABLE_BLOCKS, Entry.SITTABLE_BLOCKS.defaultValue());
		defaultConfig.setComment(Fields.SITTABLE_BLOCKS, Entry.SITTABLE_BLOCKS.comment());

		defaultConfig.set(Fields.SITTABLE_TAGS, Entry.SITTABLE_TAGS.defaultValue());
		defaultConfig.setComment(Fields.SITTABLE_TAGS, Entry.SITTABLE_TAGS.comment());

		defaultConfig.set(Fields.SIT_PLAYERS, Entry.SIT_PLAYERS.defaultValue());
		defaultConfig.setComment(Fields.SIT_PLAYERS, Entry.SIT_PLAYERS.comment());

		return defaultConfig.unmodifiable();
	}

	public interface Fields {
		String CONFIG_VERSION = "config_version";
		String SNEAK_SIT = "sneak.sneak_sit";
		String MIN_ANGLE = "sneak.min_angle";
		String SNEAK_DELAY = "sneak.sneak_delay";
		String SITTABLE_BLOCKS = "sittable.blocks";
		String SITTABLE_TAGS = "sittable.tags";
		String SIT_PLAYERS = "misc.sit_players";
	}

	public interface Entry {
		ConfigEntry<Integer> CONFIG_VERSION = new ConfigEntry<>(Fields.CONFIG_VERSION, 3, "Do not edit");
		ConfigEntry<Boolean> SNEAK_SIT = new ConfigEntry<>(Fields.SNEAK_SIT, true, "Toggles sit-on-sneak feature");
		ConfigEntry<Double> MIN_ANGLE = new ConfigEntry<>(Fields.MIN_ANGLE, 66d, "Minimal pitch to sitting down");
		ConfigEntry<Integer> SNEAK_DELAY = new ConfigEntry<>(Fields.SNEAK_DELAY, 600, "Time in ms between sneaks for sitting down");
		ConfigEntry<List<String>> SITTABLE_BLOCKS = new ConfigEntry<>(Fields.SITTABLE_BLOCKS, List.of(), "List of block ids (e.g. \"oak_log\") available to sit");
		ConfigEntry<List<String>> SITTABLE_TAGS = new ConfigEntry<>(Fields.SITTABLE_TAGS, List.of("minecraft:slabs", "minecraft:stairs", "minecraft:logs"), "List of block tags");
		ConfigEntry<Boolean> SIT_PLAYERS = new ConfigEntry<>(Fields.SIT_PLAYERS, false, "Toggles sitting on other players");

		record ConfigEntry<T>(String key, T defaultValue, String comment) {
			public void save(T value) {
				FSitConfig.config.set(this.key, value);
			}

			public Text keyAsText() {
				return FSitMod.getTranslation("option", this.key);
			}

			public Text commentAsText() {
				return FSitMod.getTranslation("comment", this.key);
			}
		}
	}

	private static class Identifier2StringConverter implements Converter<List<Identifier>, List<String>> {

		@Override
		public List<Identifier> convertToField(List<String> value) {
			return value.stream().map(Identifier::new).toList();
		}

		@Override
		public List<String> convertFromField(List<Identifier> value) {
			return value.stream().map(Identifier::toString).toList();
		}
	}
}
