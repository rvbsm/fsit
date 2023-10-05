package dev.rvbsm.fsit.entity;

import dev.rvbsm.fsit.config.ConfigData;
import net.minecraft.entity.player.PlayerEntity;

public interface ConfigHandler {

	ConfigData fsit$getConfig();

	void fsit$setConfig(ConfigData config);

	boolean fsit$hasConfig();

	boolean fsit$canStartRiding(PlayerEntity player);
}
