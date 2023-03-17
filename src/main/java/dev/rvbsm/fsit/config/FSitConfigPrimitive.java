package dev.rvbsm.fsit.config;

import java.util.Set;

public class FSitConfigPrimitive {
	private final Double min_angle;
	private final Long shift_delay;
	private final Set<String> sittable_blocks;
	private final Set<String> sittable_tags;

	public FSitConfigPrimitive(Double min_angle, Long shift_delay, Set<String> sittable_blocks, Set<String> sittable_tags) {
		this.min_angle = min_angle;
		this.shift_delay = shift_delay;
		this.sittable_blocks = sittable_blocks;
		this.sittable_tags = sittable_tags;
	}

	public void fromPrimitive() {
		FSitConfig.minAngle.setValue(this.min_angle);
		FSitConfig.shiftDelay.setValue(this.shift_delay);
		FSitConfig.sittableBlocks.setValue(this.sittable_blocks);
		FSitConfig.sittableTags.setValue(this.sittable_tags);
	}
}
