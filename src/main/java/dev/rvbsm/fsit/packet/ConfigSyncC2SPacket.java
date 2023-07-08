package dev.rvbsm.fsit.packet;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.config.ConfigData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public record ConfigSyncC2SPacket(ConfigData config) implements FabricPacket {

	public static final PacketType<ConfigSyncC2SPacket> TYPE = PacketType.create(new Identifier("fsit", "config_sync"), ConfigSyncC2SPacket::new);

	public ConfigSyncC2SPacket(PacketByteBuf buf) {
		this(new ConfigData(buf.readBoolean(), buf.readDouble(), buf.readInt()));
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeBoolean(this.config.sneak);
		buf.writeDouble(this.config.minAngle);
		buf.writeInt(this.config.sneakDelay);
	}

	@Environment(EnvType.SERVER)
	public void receive(ServerPlayerEntity player, PacketSender packetSender) {
		FSitMod.setModded(player.getUuid(), this.config);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
