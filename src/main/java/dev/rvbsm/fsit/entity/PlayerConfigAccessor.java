package dev.rvbsm.fsit.entity;

import dev.rvbsm.fsit.config.ConfigData;

public interface PlayerConfigAccessor {

	ConfigData fsit$getConfig();

	void fsit$setConfig(ConfigData config);

	boolean fsit$isModded();
}
