package dev.rvbsm.fsit.entity;

import net.minecraft.util.math.Vec3d;

public interface PlayerPoseAccessor {

	PlayerPose fsit$getPose();

	void fsit$setPose(PlayerPose pose);

	default void resetPose() {
		this.fsit$setPose(PlayerPose.NONE);
	}

	default boolean isInPose(PlayerPose pose) {
		return this.fsit$getPose() == pose;
	}

	default boolean isPosing() {
		return !this.isInPose(PlayerPose.NONE) && !this.isInPose(PlayerPose.SNEAK);
	}

	void fsit$setSneaked();

	void fsit$setSitting();

	void fsit$setSitting(Vec3d pos);

	void fsit$setCrawling();
}
