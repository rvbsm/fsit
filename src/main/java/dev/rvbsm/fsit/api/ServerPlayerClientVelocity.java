package dev.rvbsm.fsit.api;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public interface ServerPlayerClientVelocity {
    @NotNull Vec3d fsit$getClientVelocity();
    void fsit$setClientVelocity(@NotNull Vec3d velocity);
}
