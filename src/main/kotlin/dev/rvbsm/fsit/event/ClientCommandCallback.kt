package dev.rvbsm.fsit.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
import net.minecraft.server.network.ServerPlayerEntity

fun interface ClientCommandCallback {
    fun onClientMode(player: ServerPlayerEntity, mode: ClientCommandC2SPacket.Mode)

    companion object {
        @JvmField
        val EVENT: Event<ClientCommandCallback> =
            EventFactory.createArrayBacked(ClientCommandCallback::class.java) { listeners ->
                ClientCommandCallback { player, mode ->
                    for (listener in listeners) {
                        listener.onClientMode(player, mode)
                    }
                }
            }
    }
}
