package dev.rvbsm.fsit;

import dev.rvbsm.fsit.config.PlayerBlockList;
import dev.rvbsm.fsit.event.client.InteractBlockCallback;
import dev.rvbsm.fsit.event.client.InteractPlayerCallback;
import dev.rvbsm.fsit.packet.PingS2CPacket;
import dev.rvbsm.fsit.packet.PongC2SPacket;
import dev.rvbsm.fsit.packet.RidePlayerC2SPacket;
import dev.rvbsm.fsit.packet.RidePlayerS2CPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

public class FSitClientMod implements ClientModInitializer {

	public static final PlayerBlockList blocklist = new PlayerBlockList(FSitMod.getModId() + "-blocklist");

	@Override
	public void onInitializeClient() {
		blocklist.load();

		UseBlockCallback.EVENT.register(InteractBlockCallback::interactBlock);
		UseEntityCallback.EVENT.register(InteractPlayerCallback::interactPlayer);

		ClientPlayNetworking.registerGlobalReceiver(PongC2SPacket.PING_PACKET, PingS2CPacket::receive);
		ClientPlayNetworking.registerGlobalReceiver(RidePlayerC2SPacket.RIDE_PLAYER_PACKET, RidePlayerS2CPacket::receiveRequest);
	}
}
