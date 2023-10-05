package dev.rvbsm.fsit;

import dev.rvbsm.fsit.network.ClientNetworkHandler;
import dev.rvbsm.fsit.network.packet.ConfigSyncC2SPacket;
import dev.rvbsm.fsit.network.packet.PoseSyncS2CPacket;
import dev.rvbsm.fsit.network.packet.RestrictionListSyncS2CPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public final class FSitModClient implements ClientModInitializer {

	static void saveConfig() {
		FSitMod.getConfigManager().saveConfig();

		if (MinecraftClient.getInstance().getServer() != null)
			ClientPlayNetworking.send(new ConfigSyncC2SPacket(FSitMod.getConfig()));
	}

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(RestrictionListSyncS2CPacket.TYPE, ClientNetworkHandler::receiveRestrictionList);
		ClientPlayNetworking.registerGlobalReceiver(PoseSyncS2CPacket.TYPE, ClientNetworkHandler::receivePoseSync);
	}
}
