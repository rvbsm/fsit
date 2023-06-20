package dev.rvbsm.fsit.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record PingS2CPacket() implements FabricPacket {

	public static final PacketType<PingS2CPacket> TYPE = PacketType.create(new Identifier("fsit", "ping"), PingS2CPacket::new);

	private PingS2CPacket(PacketByteBuf buf) {
		this();
	}

	@Override
	public void write(PacketByteBuf buf) {
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
