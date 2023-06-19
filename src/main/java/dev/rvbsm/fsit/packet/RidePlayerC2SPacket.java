package dev.rvbsm.fsit.packet;

import dev.rvbsm.fsit.FSitMod;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.UUID;

public abstract class RidePlayerC2SPacket {

	public static final Identifier RIDE_PLAYER_PACKET = new Identifier(FSitMod.getModId(), "ride_player");
	public static final Identifier RIDE_ACCEPT_PACKET = new Identifier(FSitMod.getModId(), "ride_accept");

	public static void receiveRequest(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		final UUID targetUid = buf.readUuid();
		if (!FSitMod.isModded(targetUid)) return;

		final ServerPlayerEntity target = server.getPlayerManager().getPlayer(targetUid);
		if (target == null || target.equals(player)) return;

		final PacketByteBuf requestBuf = PacketByteBufs.create();
		requestBuf.writeUuid(player.getUuid());
		ServerPlayNetworking.send(target, RIDE_PLAYER_PACKET, requestBuf);
	}

	public static void receiveAccept(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		final UUID issuerUid = buf.readUuid();
		final ServerPlayerEntity issuer = server.getPlayerManager().getPlayer(issuerUid);

		if (issuer != null && player.distanceTo(issuer) <= 3) issuer.startRiding(player);
	}

	public static void sendRequest(PlayerEntity target, PlayerEntity issuer) {
		final PacketByteBuf buf = PacketByteBufs.create();
		buf.writeUuid(issuer.getUuid());
		ServerPlayNetworking.send((ServerPlayerEntity) target, RIDE_PLAYER_PACKET, buf);
	}
}
