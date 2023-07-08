package dev.rvbsm.fsit.packet;

import dev.rvbsm.fsit.entity.PlayerPoseAccessor;
import dev.rvbsm.fsit.event.InteractBlockCallback;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
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

	public void receive(ServerPlayerEntity player, PacketSender responseSender) {
		if (InteractBlockCallback.isInRadius(this.playerPos, this.sitPos))
			((PlayerPoseAccessor) player).setPlayerSitting(this.sitPos);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
