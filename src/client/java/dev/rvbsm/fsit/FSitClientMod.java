package dev.rvbsm.fsit;

import dev.rvbsm.fsit.event.client.InteractBlockCallback;
import dev.rvbsm.fsit.packet.PingS2CPacket;
import dev.rvbsm.fsit.packet.PongC2SPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

public class FSitClientMod implements ClientModInitializer {
	@Override
	public void onInitializeClient() {

		UseBlockCallback.EVENT.register(InteractBlockCallback::interactBlock);

		ClientPlayNetworking.registerGlobalReceiver(PongC2SPacket.PING_PACKET, PingS2CPacket::receive);
	}
}
