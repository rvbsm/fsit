package dev.rvbsm.fsit.api.network;

import org.jetbrains.annotations.NotNull;

import dev.rvbsm.fsit.networking.payload.RidingResponseC2SPayload;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface RidingRequestHandler {

    @NotNull CompletableFuture<Boolean> fsit$newRidingRequest(@NotNull UUID playerUUID, @NotNull Duration duration);

    void fsit$completeRidingRequest(@NotNull RidingResponseC2SPayload response);
}
