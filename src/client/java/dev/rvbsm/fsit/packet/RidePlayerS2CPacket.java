package dev.rvbsm.fsit.packet;

import dev.rvbsm.fsit.FSitClientMod;
import dev.rvbsm.fsit.FSitMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public abstract class RidePlayerS2CPacket {

	public static void receiveRequest(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		if (!FSitMod.config.sitPlayers) return;

		final UUID issuerUid = buf.readUuid();
		if (FSitClientMod.blockedPlayers.contains(issuerUid)) return;

		final PacketByteBuf responseBuf = PacketByteBufs.create();
		responseBuf.writeUuid(issuerUid);
		responseSender.sendPacket(RidePlayerC2SPacket.RIDE_ACCEPT_PACKET, responseBuf);
	}

	public static void sendRequest(PlayerEntity target) {
		final PacketByteBuf buf = PacketByteBufs.create();
		buf.writeUuid(target.getUuid());
		ClientPlayNetworking.send(RidePlayerC2SPacket.RIDE_PLAYER_PACKET, buf);
	}
}
