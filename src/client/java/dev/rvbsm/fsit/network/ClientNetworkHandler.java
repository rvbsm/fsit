package dev.rvbsm.fsit.network;

import dev.rvbsm.fsit.entity.PoseHandler;
import dev.rvbsm.fsit.entity.RestrictHandler;
import dev.rvbsm.fsit.network.packet.PoseSyncS2CPacket;
import dev.rvbsm.fsit.network.packet.RestrictionListSyncS2CPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.network.ClientPlayerEntity;

public class ClientNetworkHandler {

	public static void receivePoseSync(PoseSyncS2CPacket packet, ClientPlayerEntity player, PacketSender sender) {
		((PoseHandler) player).fsit$setPose(packet.pose());
	}

	public static void receiveRestrictionList(RestrictionListSyncS2CPacket packet, ClientPlayerEntity player, PacketSender sender) {
		packet.restrictList().forEach(((RestrictHandler) player)::fsit$restrictPlayer);
	}
}
