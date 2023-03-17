package dev.rvbsm.fsit.config;

import dev.rvbsm.fsit.config.option.BlockSetOption;
import dev.rvbsm.fsit.config.option.DoubleOption;
import dev.rvbsm.fsit.config.option.LongOption;
import dev.rvbsm.fsit.config.option.TagKeyBlockSetOption;

import java.util.Set;

public class FSitConfig {
	public static final DoubleOption minAngle = new DoubleOption("min_angle", 66.6D);
	public static final LongOption shiftDelay = new LongOption("shift_delay", 600L);
	public static final BlockSetOption sittableBlocks = new BlockSetOption("sittable_blocks", Set.of());
	public static final TagKeyBlockSetOption sittableTags = new TagKeyBlockSetOption("sittable_tags", Set.of("slabs", "stairs", "logs"));

	public static FSitConfigPrimitive toPrimitive() {
		return new FSitConfigPrimitive(minAngle.getValue(), shiftDelay.getValue(), sittableBlocks.getValue(), sittableTags.getValue());
	}
}
