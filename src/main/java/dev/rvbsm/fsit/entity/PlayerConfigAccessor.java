package dev.rvbsm.fsit.entity;

import dev.rvbsm.fsit.config.ConfigData;

public interface PlayerConfigAccessor {

	ConfigData getConfig();

	void setConfig(ConfigData config);

	boolean isModded();
}
