package dev.rvbsm.fsit.api.network;

import net.minecraft.util.math.Vec3d;

import org.jetbrains.annotations.NotNull;

public interface ServerPlayerVelocity {

    @NotNull Vec3d fsit$getPlayerVelocity();
}
