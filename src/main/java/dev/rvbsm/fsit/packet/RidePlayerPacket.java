package dev.rvbsm.fsit.packet;

import dev.rvbsm.fsit.config.ConfigData;
import dev.rvbsm.fsit.entity.PlayerConfigAccessor;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
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

	public void receive(ServerPlayerEntity player, PacketSender responseSender) {
		final ServerPlayerEntity target = (ServerPlayerEntity) player.getServerWorld().getPlayerByUuid(this.uuid);
		final PlayerConfigAccessor configAccessor = (PlayerConfigAccessor) target;
		if (target == null) return;

		final ConfigData config = configAccessor.fsit$getConfig();
		switch (this.type) {
			case REQUEST -> {
				if (configAccessor.fsit$isModded())
					ServerPlayNetworking.send(target, new RidePlayerPacket(this.type, player.getUuid()));
				else if (config.ridePlayers) player.startRiding(target, true);
			}
			case ACCEPT -> {
				if (player.distanceTo(target) <= config.rideRadius) target.startRiding(player, true);
			}
			case REFUSE -> {
				if (player.hasPassenger(target)) target.stopRiding();
				else if (target.hasPassenger(player)) player.stopRiding();
			}
		}
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}

	public enum RideType {
		REQUEST, ACCEPT, REFUSE
	}
}
