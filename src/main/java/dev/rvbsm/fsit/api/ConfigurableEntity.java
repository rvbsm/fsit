package dev.rvbsm.fsit.api;

import dev.rvbsm.fsit.config.ModConfig;
import org.jetbrains.annotations.NotNull;

public interface ConfigurableEntity {
    void fsit$setConfig(@NotNull ModConfig config);

    @NotNull ModConfig fsit$getConfig();

    boolean fsit$hasConfig();
}
