package dev.rvbsm.fsit.compat

abstract class CustomPayload(@JvmField val id: Id) :
    /*? if >=1.20.5- {*//*
    net.minecraft.network.packet.CustomPayload {
    override fun getId() = id

    abstract fun write(buf: net.minecraft.network.PacketByteBuf)
    *//*?} else {*/
    net.fabricmc.fabric.api.networking.v1.FabricPacket {
    override fun getType() = id
    /*?} */
}

private typealias Id =
        /*? if >=1.20.5- {*//*
        net.minecraft.network.packet.CustomPayload.Id<*>
        *//*?} else {*/
        net.fabricmc.fabric.api.networking.v1.PacketType<*>
        /*?} */
