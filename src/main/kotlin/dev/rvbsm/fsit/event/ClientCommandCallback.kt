package dev.rvbsm.fsit.event

import dev.rvbsm.fsit.entity.PlayerPose
import dev.rvbsm.fsit.network.getConfig
import dev.rvbsm.fsit.network.setPose
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.EntityPose
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Direction
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

private val sneaks = mutableMapOf<UUID, CompletableFuture<Boolean>>()

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
            if (mode != ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY || !player.isOnGround) return

            val config = player.getConfig().onDoubleSneak.takeUnless { !it.sitting && !it.crawling } ?: return
            if (player.pitch < config.minPitch) return

            if (player.uuid !in sneaks) {
                createSneak(player.uuid, config.delay).thenAccept {
                    when {
                        it && config.crawling && player.isNearGap() -> player.setPose(PlayerPose.Crawling)
                        it && config.sitting -> player.setPose(PlayerPose.Sitting)
                    }
                }
            } else {
                sneaks[player.uuid]?.complete(true)
            }
        }

        private fun createSneak(uuid: UUID, delay: Long) = sneaks.computeIfAbsent(uuid) {
            CompletableFuture<Boolean>().completeOnTimeout(false, delay, TimeUnit.MILLISECONDS)
                .apply { thenRunAsync { sneaks.remove(it) } }
        }

        private fun ServerPlayerEntity.isNearGap(): Boolean {
            val crawlingDimensions = this.getDimensions(EntityPose.SWIMMING)
            val crouchingDimensions = this.getDimensions(EntityPose.CROUCHING)

            val expectEmptyAt = pos.add(
                Direction.fromRotation(yaw.toDouble()).offsetX * 0.1,
                0.0,
                Direction.fromRotation(yaw.toDouble()).offsetZ * 0.1,
            )
            val expectFullAt = pos.add(
                Direction.fromRotation(yaw.toDouble()).offsetX * 0.1,
                crouchingDimensions.height.toDouble(),
                Direction.fromRotation(yaw.toDouble()).offsetZ * 0.1,
            )

            return world.isSpaceEmpty(this, crawlingDimensions.getBoxAt(expectEmptyAt).contract(1.0e-6)) &&
                    !world.isSpaceEmpty(this, crawlingDimensions.getBoxAt(expectFullAt).contract(1.0e-6))
        }
    }
}
