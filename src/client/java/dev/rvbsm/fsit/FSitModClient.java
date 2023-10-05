package dev.rvbsm.fsit;

import dev.rvbsm.fsit.config.BlockedUUIDList;
import dev.rvbsm.fsit.config.FSitConfig;
import dev.rvbsm.fsit.entity.PlayerPoseAccessor;
import dev.rvbsm.fsit.event.ClientBlockEvents;
import dev.rvbsm.fsit.event.ClientEntityEvents;
import dev.rvbsm.fsit.packet.ConfigSyncC2SPacket;
import dev.rvbsm.fsit.packet.PingS2CPacket;
import dev.rvbsm.fsit.packet.PoseSyncS2CPacket;
import dev.rvbsm.fsit.packet.RidePacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.UUID;

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
