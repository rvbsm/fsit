package dev.rvbsm.fsit.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record CrawlC2SPacket(boolean crawling) implements FabricPacket {

	public static final PacketType<CrawlC2SPacket> TYPE = PacketType.create(new Identifier("fsit", "crawl"), CrawlC2SPacket::new);

	private CrawlC2SPacket(PacketByteBuf buf) {
		this(buf.readBoolean());
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeBoolean(this.crawling);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
