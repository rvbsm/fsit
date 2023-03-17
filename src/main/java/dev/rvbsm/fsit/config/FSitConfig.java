package dev.rvbsm.fsit.config;

import dev.rvbsm.fsit.config.option.BlockSetOption;
import dev.rvbsm.fsit.config.option.SimpleOption;
import dev.rvbsm.fsit.config.option.TagKeyBlockSetOption;

import java.util.Set;

public class FSitConfig {
	public final SimpleOption<Double> minAngle = new SimpleOption<>("min_angle", 66.6D);
	public final SimpleOption<Long> shiftDelay = new SimpleOption<>("shift_delay", 600L);
	public final BlockSetOption sittableBlocks = new BlockSetOption("sittable_blocks", Set.of());
	public final TagKeyBlockSetOption sittableTags = new TagKeyBlockSetOption("sittable_tags", Set.of("slabs", "stairs", "logs"));

	protected FSitConfig() {
	}

	public FSitConfig(Double minAngle, Long shiftDelay, Set<String> sittableBlocks, Set<String> sittableTags) {
		this.minAngle.setValue(minAngle);
		this.shiftDelay.setValue(shiftDelay);
		this.sittableBlocks.setValue(sittableBlocks);
		this.sittableTags.setValue(sittableTags);
	}

	public FSitConfigSimple toSimple() {
		return new FSitConfigSimple(minAngle.getValue(), shiftDelay.getValue(), sittableBlocks.getValue(), sittableTags.getValue());
	}
}
