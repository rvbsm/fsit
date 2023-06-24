package dev.rvbsm.fsit.packet;

import dev.rvbsm.fsit.entity.PlayerPose;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record PoseSyncS2CPacket(PlayerPose pose) implements FabricPacket {

	public static final PacketType<PoseSyncS2CPacket> TYPE = PacketType.create(new Identifier("fsit", "pose_sync"), PoseSyncS2CPacket::new);

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
