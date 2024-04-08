package dev.rvbsm.fsit.event

import dev.rvbsm.fsit.entity.Pose
import dev.rvbsm.fsit.network.getConfig
import dev.rvbsm.fsit.network.setPose
import kotlinx.coroutines.*
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
import net.minecraft.server.network.ServerPlayerEntity
import java.util.*

private val scope = CoroutineScope(Dispatchers.IO)
private val sneaks = mutableMapOf<UUID, Job>()

fun interface ClientCommandCallback {
    fun onClientMode(player: ServerPlayerEntity, mode: ClientCommandC2SPacket.Mode)

    companion object : ClientCommandCallback {
        @JvmField
        val EVENT: Event<ClientCommandCallback> =
            EventFactory.createArrayBacked(ClientCommandCallback::class.java) { listeners ->
                ClientCommandCallback { player, mode ->
                    for (listener in listeners) {
                        listener.onClientMode(player, mode)
                    }
                }
            }

        override fun onClientMode(player: ServerPlayerEntity, mode: ClientCommandC2SPacket.Mode) {
            if (mode != ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY) return
            val playerConfig = player.getConfig()

            if (playerConfig.sitting.onDoubleSneak.enabled && player.pitch > playerConfig.sitting.onDoubleSneak.minPitch) {
                if (player.uuid !in sneaks) {
                    sneaks[player.uuid] = scope.launch {
                        delay(playerConfig.sitting.onDoubleSneak.delay)

                        sneaks.remove(player.uuid)
                    }
                } else {
                    sneaks[player.uuid]?.cancel()
                    sneaks.remove(player.uuid)

                    player.setPose(Pose.Sitting)
                }
            }
        }
    }
}