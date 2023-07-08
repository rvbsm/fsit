package dev.rvbsm.fsit.entity;

import net.minecraft.util.math.Vec3d;

public interface PlayerPoseAccessor {

	PlayerPose getPlayerPose();

	void setPlayerPose(PlayerPose pose);

	default void resetPlayerPose() {
		this.setPlayerPose(PlayerPose.NONE);
	}

	default boolean isInPlayerPose(PlayerPose pose) {
		return this.getPlayerPose() == pose;
	}

	default boolean isPlayerPosing() {
		return !this.isInPlayerPose(PlayerPose.NONE) && !this.isInPlayerPose(PlayerPose.SNEAK);
	}

	void setPlayerSneaked();

	void setPlayerSitting();

	void setPlayerSitting(Vec3d pos);

	void setPlayerCrawling();
}
