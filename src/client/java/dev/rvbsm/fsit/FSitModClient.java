package dev.rvbsm.fsit;

import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.config.FSitConfig;
import dev.rvbsm.fsit.config.PlayerBlockList;
import dev.rvbsm.fsit.entity.PlayerPoseAccessor;
import dev.rvbsm.fsit.event.client.InteractBlockCallback;
import dev.rvbsm.fsit.event.client.InteractPlayerCallback;
import dev.rvbsm.fsit.packet.ConfigSyncC2SPacket;
import dev.rvbsm.fsit.packet.PingS2CPacket;
import dev.rvbsm.fsit.packet.PoseSyncS2CPacket;
import dev.rvbsm.fsit.packet.RidePlayerPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.MinecraftClient;

public class FSitModClient implements ClientModInitializer {

	public static final PlayerBlockList blockedPlayers = new PlayerBlockList("fsit-blocklist");
	public static final ConfigData config = FSitMod.config;

	protected static void saveConfig() {
		FSitConfig.save(FSitMod.config);

		if (MinecraftClient.getInstance().getServer() != null)
			ClientPlayNetworking.send(new ConfigSyncC2SPacket(FSitMod.config));
	}

	@Override
	public void onInitializeClient() {
		blockedPlayers.load();

		UseBlockCallback.EVENT.register(InteractBlockCallback::interactBlock);
		UseEntityCallback.EVENT.register(InteractPlayerCallback::interactPlayer);

		ClientPlayNetworking.registerGlobalReceiver(PingS2CPacket.TYPE, (packet, player, responseSender) -> responseSender.sendPacket(new ConfigSyncC2SPacket(FSitModClient.config)));
		ClientPlayNetworking.registerGlobalReceiver(PoseSyncS2CPacket.TYPE, (packet, player, responseSender) -> ((PlayerPoseAccessor) player).fsit$setPose(packet.pose()));
		ClientPlayNetworking.registerGlobalReceiver(RidePlayerPacket.TYPE, (packet, player, responseSender) -> {
			if (packet.type() == RidePlayerPacket.RideType.REQUEST)
				if (FSitModClient.config.ridePlayers && !FSitModClient.blockedPlayers.contains(packet.uuid()))
					responseSender.sendPacket(new RidePlayerPacket(RidePlayerPacket.RideType.ACCEPT, packet.uuid()));
		});
	}
}
