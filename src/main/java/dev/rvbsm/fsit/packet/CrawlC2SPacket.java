package dev.rvbsm.fsit.packet;

import dev.rvbsm.fsit.FSitMod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public abstract class CrawlC2SPacket {

	public static final Identifier CRAWL_PACKET = new Identifier(FSitMod.getModId(), "crawl");

	public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		FSitMod.addCrawled(player);
	}

	public static void send() {
		ClientPlayNetworking.send(CRAWL_PACKET, PacketByteBufs.empty());
	}
}
