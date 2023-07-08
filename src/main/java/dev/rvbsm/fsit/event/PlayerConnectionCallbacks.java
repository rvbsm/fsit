package dev.rvbsm.fsit.event;

import dev.rvbsm.fsit.entity.PlayerPoseAccessor;
import dev.rvbsm.fsit.packet.PingS2CPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public abstract class PlayerConnectionCallbacks {

	public static void onConnect(ServerPlayNetworkHandler serverPlayNetworkHandler, PacketSender packetSender, MinecraftServer minecraftServer) {
		packetSender.sendPacket(new PingS2CPacket());
		((PlayerPoseAccessor) serverPlayNetworkHandler.player).resetPlayerPose();
	}

	public static void onDisconnect(ServerPlayNetworkHandler serverPlayNetworkHandler, MinecraftServer server) {
		((PlayerPoseAccessor) serverPlayNetworkHandler.player).resetPlayerPose();
	}
}
