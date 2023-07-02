package dev.rvbsm.fsit.config;

import com.electronwill.nightconfig.core.conversion.Conversion;
import com.electronwill.nightconfig.core.conversion.Converter;
import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import com.electronwill.nightconfig.core.conversion.SpecDoubleInRange;
import com.electronwill.nightconfig.core.conversion.SpecIntInRange;
import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.config.conversion.Comment;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class ConfigData {

	@Path(Fields.CONFIG_VERSION)
	@Comment(Comments.CONFIG_VERSION)
	@PreserveNotNull
	public final int configVersion = Entries.CONFIG_VERSION.defaultValue;

	@Path(Fields.SNEAK_ENABLED)
	@Comment(Comments.SNEAK_ENABLED)
	@PreserveNotNull
	public boolean sneak = Entries.SNEAK_ENABLED.defaultValue;

	@Path(Fields.MIN_ANGLE)
	@Comment(Comments.MIN_ANGLE)
	@PreserveNotNull
	@SpecDoubleInRange(min = 0, max = 90)
	public double minAngle = Entries.MIN_ANGLE.defaultValue;

	@Path(Fields.SNEAK_DELAY)
	@Comment(Comments.SNEAK_DELAY)
	@PreserveNotNull
	@SpecIntInRange(min = 100, max = 2000)
	public int sneakDelay = Entries.SNEAK_DELAY.defaultValue;

	@Path(Fields.SITTABLE_ENABLED)
	@Comment(Comments.SITTABLE_ENABLED)
	@PreserveNotNull
	public final boolean sittable = Entries.SITTABLE_ENABLED.defaultValue;

	@Path(Fields.SITTABLE_BLOCKS)
	@Comment(Comments.SITTABLE_BLOCKS)
	@PreserveNotNull
	@Conversion(Identifier2StringConverter.class)
	public final List<Identifier> sittableBlocks = Entries.SITTABLE_BLOCKS.defaultValue.stream().map(Identifier::new).toList();

	@Path(Fields.SITTABLE_TAGS)
	@Comment(Comments.SITTABLE_TAGS)
	@PreserveNotNull
	@Conversion(Identifier2StringConverter.class)
	public final List<Identifier> sittableTags = Entries.SITTABLE_TAGS.defaultValue.stream().map(Identifier::new).toList();

	@Path(Fields.RIDE_PLAYERS)
	@Comment(Comments.RIDE_PLAYERS)
	@PreserveNotNull
	public final boolean ridePlayers = Entries.RIDE_PLAYERS.defaultValue;

	public ConfigData() {}

	public ConfigData(boolean sneak, double minAngle, int sneakDelay) {
		this.sneak = sneak;
		this.minAngle = minAngle;
		this.sneakDelay = sneakDelay;
	}

	public interface Fields {
		String CONFIG_VERSION = "config_version";
		String SNEAK_ENABLED = "sneak.enabled";
		String MIN_ANGLE = "sneak.min_angle";
		String SNEAK_DELAY = "sneak.delay";
		String SITTABLE_ENABLED = "sittable.enabled";
		String SITTABLE_BLOCKS = "sittable.blocks";
		String SITTABLE_TAGS = "sittable.tags";
		String RIDE_PLAYERS = "misc.ride_players";
	}

	private interface Comments {
		String CONFIG_VERSION = "Do not edit";
		String SNEAK_ENABLED = "Toggles sit-on-sneak feature";
		String MIN_ANGLE = "Minimal pitch to sitting down";
		String SNEAK_DELAY = "Time in ms between sneaks for sitting down";
		String SITTABLE_ENABLED = "Toggles sitting on specified blocks";
		String SITTABLE_BLOCKS = "List of block ids (e.g. \"oak_log\") available to sit";
		String SITTABLE_TAGS = "List of block tags";
		String RIDE_PLAYERS = "Toggles riding other players";
	}

	public interface Entries {
		ConfigEntry<Integer> CONFIG_VERSION = new ConfigEntry<>(Fields.CONFIG_VERSION, 3);
		ConfigEntry<Boolean> SNEAK_ENABLED = new ConfigEntry<>(Fields.SNEAK_ENABLED, true);
		ConfigEntry<Double> MIN_ANGLE = new ConfigEntry<>(Fields.MIN_ANGLE, 66d);
		ConfigEntry<Integer> SNEAK_DELAY = new ConfigEntry<>(Fields.SNEAK_DELAY, 600);
		ConfigEntry<Boolean> SITTABLE_ENABLED = new ConfigEntry<>(Fields.SITTABLE_ENABLED, true);
		ConfigEntry<List<String>> SITTABLE_BLOCKS = new ConfigEntry<>(Fields.SITTABLE_BLOCKS, List.of());
		ConfigEntry<List<String>> SITTABLE_TAGS = new ConfigEntry<>(Fields.SITTABLE_TAGS, List.of("minecraft:slabs", "minecraft:stairs", "minecraft:logs"));
		ConfigEntry<Boolean> RIDE_PLAYERS = new ConfigEntry<>(Fields.RIDE_PLAYERS, false);

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
