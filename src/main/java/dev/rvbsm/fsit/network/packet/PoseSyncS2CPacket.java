package dev.rvbsm.fsit.network.packet;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.entity.PlayerPose;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record PoseSyncS2CPacket(PlayerPose pose) implements FabricPacket {

	private static final Identifier ID = Identifier.of(FSitMod.MOD_ID, "pose_sync");
	public static final PacketType<PoseSyncS2CPacket> TYPE = PacketType.create(ID, PoseSyncS2CPacket::new);

	private PoseSyncS2CPacket(PacketByteBuf buf) {
		this(buf.readEnumConstant(PlayerPose.class));
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeEnumConstant(this.pose);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
