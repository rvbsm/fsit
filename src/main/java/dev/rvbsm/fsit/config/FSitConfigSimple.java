package dev.rvbsm.fsit.config;

import java.util.Set;

public class FSitConfigSimple {
	private final Double min_angle;
	private final Long shift_delay;
	private final Set<String> sittable_blocks;
	private final Set<String> sittable_tags;

	public FSitConfigSimple(Double min_angle, Long shift_delay, Set<String> sittable_blocks, Set<String> sittable_tags) {
		this.min_angle = min_angle;
		this.shift_delay = shift_delay;
		this.sittable_blocks = sittable_blocks;
		this.sittable_tags = sittable_tags;
	}

	public FSitConfig cast() {
		return new FSitConfig(this.min_angle, this.shift_delay, this.sittable_blocks, this.sittable_tags);
	}
}
