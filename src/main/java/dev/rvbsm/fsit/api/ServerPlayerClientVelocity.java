package dev.rvbsm.fsit.api;

import net.minecraft.util.math.Vec3d;

public interface ServerPlayerClientVelocity {
    Vec3d fsit$getClientVelocity();
    void fsit$setClientVelocity(Vec3d velocity);
}
