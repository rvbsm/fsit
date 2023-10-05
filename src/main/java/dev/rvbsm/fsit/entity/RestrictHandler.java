package dev.rvbsm.fsit.entity;

import net.minecraft.entity.player.PlayerEntity;

import java.util.Set;
import java.util.UUID;

public interface RestrictHandler {

	Set<UUID> fsit$getRestrictionList();

	void fsit$restrictPlayer(UUID playerUUID);

	void fsit$allowPlayer(UUID playerUUID);

	boolean fsit$isRestricted(UUID playerUUID);
}
