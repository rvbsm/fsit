package dev.rvbsm.fsit.networking.payload

import dev.rvbsm.fsit.FSitMod
import net.minecraft.network.NetworkSide
import net.minecraft.network.PacketByteBuf

abstract class CustomPayload<P : CustomPayload<P>>(
    @JvmField val id: /*$ CustomPayload.Id >>*/ net.fabricmc.fabric.api.networking.v1.PacketType<P>
) : /*$ CustomPayload >>*/ net.fabricmc.fabric.api.networking.v1.FabricPacket {

    //? if <=1.20.4 {
    override fun getType() = id
    //?} else if >=1.20.5 {
    /*override fun getId() = id
    abstract fun write(buf: PacketByteBuf)
    *///?}

    abstract class Id<P : CustomPayload<P>>(path: String, side: NetworkSide) {
        private val id = FSitMod.id(path)

        //? if <=1.20.4 {
        val packetId: net.fabricmc.fabric.api.networking.v1.PacketType<P> =
            net.fabricmc.fabric.api.networking.v1.PacketType<P>.create(id, ::init)
        //?} else if >=1.20.5 {
        /*private val registry = when (side) {
            NetworkSide.SERVERBOUND -> net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playC2S()
            NetworkSide.CLIENTBOUND -> net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
        }

        val packetId = net.minecraft.network.packet.CustomPayload.Id<P>(id)
        private val packetCodec = net.minecraft.network.packet.CustomPayload.codecOf(CustomPayload<P>::write, ::init)

        init {
            registry.register(packetId, packetCodec)
        }
        *///?}

        internal abstract fun init(buf: PacketByteBuf): P
    }
}
