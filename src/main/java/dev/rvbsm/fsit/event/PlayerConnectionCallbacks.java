package dev.rvbsm.fsit.event;

import dev.rvbsm.fsit.entity.SeatEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.jetbrains.annotations.NotNull;

public abstract class PlayerConnectionCallbacks {

	public static void onDisconnect(@NotNull ServerPlayNetworkHandler serverPlayNetworkHandler, MinecraftServer ignored) {
		final Entity vehicle = serverPlayNetworkHandler.player.getVehicle();
		if (vehicle instanceof SeatEntity || vehicle instanceof PlayerEntity) serverPlayNetworkHandler.player.stopRiding();
	}

}
