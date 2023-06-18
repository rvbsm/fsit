package dev.rvbsm.fsit.packet;

import dev.rvbsm.fsit.FSitMod;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public abstract class PongC2SPacket {

	public static final Identifier PING_PACKET = new Identifier(FSitMod.getModId(), "ping");
	public static final Identifier PONG_PACKET = new Identifier(FSitMod.getModId(), "pong");

	public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		FSitMod.addModded(player.getUuid());
	}
}
