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

	private static void receivePoseSync(PoseSyncS2CPacket packet, ClientPlayerEntity player, PacketSender responseSender) {
		((PlayerPoseAccessor) player).fsit$setPose(packet.pose());
	}

	private static void receiveRide(RidePacket packet, ClientPlayerEntity player, PacketSender responseSender) {
		if (packet.type() == RidePacket.ActionType.REQUEST) {
			if (FSitMod.config.ride && !FSitModClient.blockedRiders.contains(packet.uuid()))
				responseSender.sendPacket(new RidePacket(RidePacket.ActionType.ACCEPT, packet.uuid()));
		}
	}

	@Override
	public void onInitializeClient() {
		UseBlockCallback.EVENT.register(ClientBlockEvents::useOnBlock);
		UseEntityCallback.EVENT.register(ClientEntityEvents::useOnPlayer);

		ClientPlayNetworking.registerGlobalReceiver(PoseSyncS2CPacket.TYPE, FSitModClient::receivePoseSync);
		ClientPlayNetworking.registerGlobalReceiver(RidePacket.TYPE, FSitModClient::receiveRide);
	}
}
