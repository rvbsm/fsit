package dev.rvbsm.fsit.api.player;

import org.jetbrains.annotations.NotNull;

import dev.rvbsm.fsit.config.ModConfig;

public interface PlayerConfig {

    void fsit$setConfig(@NotNull ModConfig config);

    @NotNull ModConfig fsit$getConfig();

    boolean fsit$hasConfig();
}
