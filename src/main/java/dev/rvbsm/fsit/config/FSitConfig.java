package dev.rvbsm.fsit.config;

import dev.rvbsm.fsit.config.option.BlockSetOption;
import dev.rvbsm.fsit.config.option.Option;
import dev.rvbsm.fsit.config.option.TagKeyBlockSetOption;

import java.util.List;

public class FSitConfig {
	public static final Option<Double> minAngle = new Option<>("min_angle", 66.6D);
	public static final Option<Long> shiftDelay = new Option<>("shift_delay", 600L);
	public static final BlockSetOption sittableBlocks = new BlockSetOption("sittable_blocks", List.of());
	public static final TagKeyBlockSetOption sittableTags = new TagKeyBlockSetOption("sittable_tags", List.of("slabs", "stairs", "logs"));

	public static FSitConfigPrimitive toPrimitive() {
		return new FSitConfigPrimitive(minAngle.getValue(), shiftDelay.getValue(), sittableBlocks.getValue(), sittableTags.getValue());
	}
}
