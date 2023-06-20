package dev.rvbsm.fsit.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record RidePlayerPacket(RideType type, UUID uuid) implements FabricPacket {

	public static final PacketType<RidePlayerPacket> TYPE = PacketType.create(new Identifier("fsit", "ride"), RidePlayerPacket::new);

	private RidePlayerPacket(PacketByteBuf buf) {
		this(buf.readEnumConstant(RideType.class), buf.readUuid());
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeEnumConstant(this.type);
		buf.writeUuid(this.uuid);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}

	public enum RideType {
		REQUEST, ACCEPT, REFUSE
	}
}
