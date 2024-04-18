package dev.rvbsm.fsit.event

import dev.rvbsm.fsit.network.getConfig
import dev.rvbsm.fsit.network.packet.RidingRequestS2CPayload
import dev.rvbsm.fsit.network.packet.RidingResponseC2SPayload
import dev.rvbsm.fsit.network.sendIfPossible
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.entity.Entity
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

private const val TIMEOUT = 5000L
private val requests = mutableMapOf<Pair<UUID, UUID>, CompletableFuture<Boolean>>()

fun interface PassedUseEntityCallback {
    fun interactEntity(player: ServerPlayerEntity, world: ServerWorld, entity: Entity): ActionResult

    companion object : PassedUseEntityCallback, ServerLifecycleEvents.ServerStopping {
        @JvmField
        val EVENT: Event<PassedUseEntityCallback> =
            EventFactory.createArrayBacked(PassedUseEntityCallback::class.java) { listeners ->
                PassedUseEntityCallback { player, world, entity ->
                    for (listener in listeners) {
                        val result = listener.interactEntity(player, world, entity)

                        if (result != ActionResult.PASS) {
                            return@PassedUseEntityCallback result
                        }
                    }

                    return@PassedUseEntityCallback ActionResult.PASS
                }
            }

        override fun interactEntity(player: ServerPlayerEntity, world: ServerWorld, entity: Entity): ActionResult {
            if (!entity.isPlayer) return ActionResult.PASS

            val target = entity as ServerPlayerEntity
            if (!player.canStartRiding(target)) return ActionResult.PASS

            val playerConfig = player.getConfig().onUse
            val targetConfig = target.getConfig().onUse

            if (player.uuid to target.uuid in requests ||
                !playerConfig.riding || !targetConfig.riding
                || !player.isInRange(target, playerConfig.range.toDouble())
            ) return ActionResult.PASS

            sendRequests(player, target, world)
            return ActionResult.PASS
        }

        override fun onServerStopping(server: MinecraftServer) {
            requests.forEach { it.value.complete(false) }
        }

        private fun sendRequest(player: ServerPlayerEntity, target: ServerPlayerEntity) =
            requests.computeIfAbsent(player.uuid to target.uuid) { pair ->
                CompletableFuture<Boolean>().completeOnTimeout(false, TIMEOUT, TimeUnit.MILLISECONDS)
                    .apply { thenRunAsync { requests.remove(pair) } }.also {
                        player.sendIfPossible(RidingRequestS2CPayload(pair.second)) { it.complete(true) }
                    }
            }

        private fun sendRequests(player: ServerPlayerEntity, target: ServerPlayerEntity, world: ServerWorld) {
            val playerRequest = sendRequest(player, target)
            val targetRequest = sendRequest(target, player)

            playerRequest.thenCombineAsync(targetRequest) { playerResult, targetResult -> playerResult && targetResult }
                .thenAccept { result ->
                    if (result && player.isAlive && target.isAlive) {
                        world.server.execute {
                            player.startRiding(target)
                            target.networkHandler.sendPacket(EntityPassengersSetS2CPacket(target))
                        }
                    }
                }
        }

        private fun ServerPlayerEntity.shouldCancelRiding() =
            shouldCancelInteraction() || isSpectator || hasPassengers()

        private fun ServerPlayerEntity.canStartRiding(other: ServerPlayerEntity) =
            this != other && uuid != other.uuid && !shouldCancelRiding() && !other.shouldCancelRiding()

        fun RidingResponseC2SPayload.completeRequest(player: ServerPlayerEntity) {
            requests[player.uuid to uuid]?.complete(response.isAccepted)
        }
    }
}
