package dev.rvbsm.fsit.api.player;

import dev.rvbsm.fsit.entity.ModPose;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PlayerPose {

    void fsit$setPose(@NotNull ModPose pose, @Nullable Vec3d pos);

    @NotNull ModPose fsit$getPose();

    @Nullable ModPose fsit$getPrevPose();

    default void fsit$resetPose() {
        this.fsit$setPose(ModPose.Standing, null);
    }

    default boolean fsit$isInPose() {
        return !this.fsit$isInPose(ModPose.Standing);
    }

    default boolean fsit$isInPose(@NotNull ModPose pose) {
        return this.fsit$getPose() == pose;
    }
}
