package dev.rvbsm.fsit.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public record SpawnSeatC2SPacket(Vec3d playerPos, Vec3d sitPos) implements FabricPacket {

	public static final PacketType<SpawnSeatC2SPacket> TYPE = PacketType.create(new Identifier("fsit", "spawn_seat"), SpawnSeatC2SPacket::new);

	public SpawnSeatC2SPacket(PacketByteBuf buf) {
		this(new Vec3d(buf.readVector3f()), new Vec3d(buf.readVector3f()));
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeVector3f(this.playerPos.toVector3f());
		buf.writeVector3f(this.sitPos.toVector3f());
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
