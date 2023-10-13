package dev.rvbsm.fsit;

import dev.rvbsm.fsit.network.ClientNetworkHandler;
import dev.rvbsm.fsit.network.packet.ConfigSyncC2SPacket;
import dev.rvbsm.fsit.network.packet.PoseSyncS2CPacket;
import dev.rvbsm.fsit.network.packet.RestrictionListSyncS2CPacket;
import lombok.Setter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class FSitModClient implements ClientModInitializer {

	@Setter
	private static boolean isConnected = false;

	static void saveConfig() {
		FSitMod.getConfigManager().saveConfig();

		if (isConnected) ClientPlayNetworking.send(new ConfigSyncC2SPacket(FSitMod.getConfig()));
	}

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(RestrictionListSyncS2CPacket.TYPE, ClientNetworkHandler::receiveRestrictionList);
		ClientPlayNetworking.registerGlobalReceiver(PoseSyncS2CPacket.TYPE, ClientNetworkHandler::receivePoseSync);

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> setConnected(false));
	}
}
