package dev.rvbsm.fsit.event;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.entity.SeatEntity;
import dev.rvbsm.fsit.packet.PingS2CPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public abstract class PlayerConnectionCallbacks {

	public static void onConnect(ServerPlayNetworkHandler serverPlayNetworkHandler, PacketSender packetSender, MinecraftServer minecraftServer) {
		packetSender.sendPacket(new PingS2CPacket());
	}

	public static void onDisconnect(ServerPlayNetworkHandler serverPlayNetworkHandler, MinecraftServer server) {
		final Entity vehicle = serverPlayNetworkHandler.player.getVehicle();
		if (vehicle instanceof SeatEntity || vehicle instanceof PlayerEntity) serverPlayNetworkHandler.player.stopRiding();

		FSitMod.removeModded(serverPlayNetworkHandler.player.getUuid());
	}

}
