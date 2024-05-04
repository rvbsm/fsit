package dev.rvbsm.fsit.api;

import dev.rvbsm.fsit.entity.PlayerPose;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Poseable {
    void fsit$setPose(@NotNull PlayerPose pose, @Nullable Vec3d pos);

    PlayerPose fsit$getPose();

    default void fsit$resetPose() {
        this.fsit$setPose(PlayerPose.Standing, null);
    }

    default boolean fsit$isInPose() {
        return !this.fsit$isInPose(PlayerPose.Standing);
    }

    default boolean fsit$isInPose(@NotNull PlayerPose pose) {
        return this.fsit$getPose() == pose;
    }
}
