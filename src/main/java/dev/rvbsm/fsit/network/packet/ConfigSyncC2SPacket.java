package dev.rvbsm.fsit.network.packet;

import dev.rvbsm.fsit.FSitMod;
import dev.rvbsm.fsit.config.ConfigData;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record ConfigSyncC2SPacket(ConfigData config) implements FabricPacket {

	private static final Identifier ID = Identifier.of(FSitMod.MOD_ID, "config_sync");
	public static final PacketType<ConfigSyncC2SPacket> TYPE = PacketType.create(ID, ConfigSyncC2SPacket::new);

	private ConfigSyncC2SPacket(PacketByteBuf buf) {
		this(FSitMod.getConfigManager().configify(buf.readString()));
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeString(FSitMod.getConfigManager().stringify(config));
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
