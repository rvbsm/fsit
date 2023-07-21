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

public record RidePacket(ActionType type, UUID uuid) implements FabricPacket {

	public static final PacketType<RidePacket> TYPE = PacketType.create(new Identifier("fsit", "ride"), RidePacket::new);

	private RidePacket(PacketByteBuf buf) {
		this(buf.readEnumConstant(ActionType.class), buf.readUuid());
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
					ServerPlayNetworking.send(target, new RidePacket(this.type, player.getUuid()));
				else if (config.ride) player.startRiding(target, true);
			}
			case ACCEPT -> target.startRiding(player, true);
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

	public enum ActionType {
		REQUEST, ACCEPT, REFUSE
	}
}
