package dev.rvbsm.fsit.api;

import dev.rvbsm.fsit.entity.Pose;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Poseable {
    void fsit$setPose(@NotNull Pose pose, @Nullable Vec3d pos);

    Pose fsit$getPose();

    default void fsit$resetPose() {
        this.fsit$setPose(Pose.Standing, null);
    }

    default boolean fsit$isInPose() {
        return !this.fsit$isInPose(Pose.Standing);
    }

    default boolean fsit$isInPose(@NotNull Pose pose) {
        return this.fsit$getPose() == pose;
    }
}
