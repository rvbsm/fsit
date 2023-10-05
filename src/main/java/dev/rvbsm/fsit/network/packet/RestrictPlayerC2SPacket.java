package dev.rvbsm.fsit.network.packet;

import dev.rvbsm.fsit.FSitMod;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.UUID;

public record RestrictPlayerC2SPacket(UUID targetUUID, Type type) implements FabricPacket {

	private static final Identifier ID = Identifier.of(FSitMod.MOD_ID, "restrict_player");
	public static final PacketType<RestrictPlayerC2SPacket> TYPE = PacketType.create(ID, RestrictPlayerC2SPacket::new);

	private RestrictPlayerC2SPacket(PacketByteBuf buf) {
		this(buf.readUuid(), buf.readEnumConstant(Type.class));
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeUuid(targetUUID);
		buf.writeEnumConstant(type);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}

	public enum Type {
		ALLOW, RESTRICT
	}
}
