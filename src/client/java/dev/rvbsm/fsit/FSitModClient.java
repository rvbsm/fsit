package dev.rvbsm.fsit;

import dev.rvbsm.fsit.config.BlockedUUIDList;
import dev.rvbsm.fsit.config.FSitConfig;
import dev.rvbsm.fsit.entity.PlayerPoseAccessor;
import dev.rvbsm.fsit.event.InteractCBlockCallback;
import dev.rvbsm.fsit.event.InteractCPlayerCallback;
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

	private static final BlockedUUIDList blockedRiders = new BlockedUUIDList("blocked-riders.fsit");

	public static void addBlockedRider(UUID uuid) {
		FSitModClient.blockedRiders.add(uuid);
	}

	public static void removeBlockedRider(UUID uuid) {
		FSitModClient.blockedRiders.remove(uuid);
	}

	public static boolean isBlockedRider(UUID uuid) {
		return FSitModClient.blockedRiders.contains(uuid);
	}

	static void saveConfig() {
		FSitConfig.save(FSitMod.config);

		if (MinecraftClient.getInstance().getServer() != null)
			ClientPlayNetworking.send(new ConfigSyncC2SPacket(FSitMod.config));
	}

	private static void receivePing(PingS2CPacket packet, ClientPlayerEntity player, PacketSender responseSender) {
		responseSender.sendPacket(new ConfigSyncC2SPacket(FSitMod.config));
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
		blockedRiders.load();

		UseBlockCallback.EVENT.register(InteractCBlockCallback::interact);
		UseEntityCallback.EVENT.register(InteractCPlayerCallback::interact);

		ClientPlayNetworking.registerGlobalReceiver(PingS2CPacket.TYPE, FSitModClient::receivePing);
		ClientPlayNetworking.registerGlobalReceiver(PoseSyncS2CPacket.TYPE, FSitModClient::receivePoseSync);
		ClientPlayNetworking.registerGlobalReceiver(RidePacket.TYPE, FSitModClient::receiveRide);
	}
}
