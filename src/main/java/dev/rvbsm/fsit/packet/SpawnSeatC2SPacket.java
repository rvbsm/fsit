package dev.rvbsm.fsit.packet;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.event.InteractBlockCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public abstract class SpawnSeatC2SPacket {

	public static final Identifier SPAWN_SEAT_PACKET = new Identifier(FSitMod.getModId(), "spawn_seat");

	public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		final Vec3d playerPos = new Vec3d(buf.readVector3f());
		if (buf.capacity() == 12) FSitMod.spawnSeat(player, player.getWorld(), playerPos);
		else {
			final Vec3d hitPos = new Vec3d(buf.readVector3f());
			final BlockPos blockPos = new BlockPos((int) hitPos.x, (int) hitPos.y, (int) hitPos.z);
			final Direction direction = buf.readEnumConstant(Direction.class);
			final boolean insideBlock = buf.readBoolean();

			final BlockHitResult hitResult = new BlockHitResult(hitPos, direction, blockPos, insideBlock);
			if (InteractBlockCallback.isInRadius(playerPos, hitResult))
				FSitMod.spawnSeat(player, player.getWorld(), hitPos);
		}
	}

	public static void send(Vec3d pos, @Nullable BlockHitResult hitResult) {
		final PacketByteBuf buf = PacketByteBufs.create();
		buf.writeVector3f(pos.toVector3f());
		if (hitResult != null) {
			buf.writeVector3f(hitResult.getPos().toVector3f());
			buf.writeEnumConstant(hitResult.getSide());
			buf.writeBoolean(hitResult.isInsideBlock());
		}

		ClientPlayNetworking.send(SPAWN_SEAT_PACKET, buf);
	}
}
