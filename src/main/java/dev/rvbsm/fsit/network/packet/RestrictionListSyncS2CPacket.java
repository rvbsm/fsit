package dev.rvbsm.fsit.network.packet;

import dev.rvbsm.fsit.FSitMod;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record RestrictionListSyncS2CPacket(Set<UUID> restrictList) implements FabricPacket {

	private static final Identifier ID = Identifier.of(FSitMod.MOD_ID, "restrict_sync");
	public static final PacketType<RestrictionListSyncS2CPacket> TYPE = PacketType.create(ID, RestrictionListSyncS2CPacket::new);

	private RestrictionListSyncS2CPacket(PacketByteBuf buf) {
		this(new HashSet<>() {{
			final int size = buf.readInt();
			for (int i = 0; i < size; i++) this.add(buf.readUuid());
		}});
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeInt(restrictList.size());
		restrictList.forEach(buf::writeUuid);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
