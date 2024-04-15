package dev.rvbsm.fsit.event

import dev.rvbsm.fsit.network.getConfig
import dev.rvbsm.fsit.network.packet.RidingRequestS2CPayload
import dev.rvbsm.fsit.network.packet.RidingResponseC2SPayload
import dev.rvbsm.fsit.network.sendIfPossible
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.world.World
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

object UseEntityListener : UseEntityCallback, ServerLifecycleEvents.ServerStopping {
    private const val TIMEOUT = 5000L
    private val requests = mutableMapOf<Pair<UUID, UUID>, CompletableFuture<Boolean>>()

    override fun interact(
        player: PlayerEntity, world: World, hand: Hand, entity: Entity, hitResult: EntityHitResult?
    ): ActionResult {
        return if (world.isClient || player.isSneaking || player.isSpectator || hitResult == null) ActionResult.PASS
        else if (!player.getStackInHand(Hand.MAIN_HAND).isEmpty || !player.getStackInHand(Hand.OFF_HAND).isEmpty) ActionResult.PASS
        else if (!player.canStartRiding(entity)) ActionResult.PASS
        else {
            val playerConfig = (player as ServerPlayerEntity).getConfig()
            val targetConfig = (entity as ServerPlayerEntity).getConfig()

            if (!playerConfig.riding.onUse.enabled || !targetConfig.riding.onUse.enabled) return ActionResult.PASS
            else if (!player.isInRange(entity, playerConfig.riding.onUse.range.toDouble())) return ActionResult.PASS

            if (!isAlreadyRequested(player, entity)) {
                val playerRequest = sendRequest(player, entity)
                val targetRequest = sendRequest(entity, player)

                playerRequest.thenAcceptBothAsync(targetRequest) { playerResult, targetResult ->
                    // note: if the player disconnects before the target accepts a ride, vice versa
                    if (playerResult && player.isAlive && targetResult && entity.isAlive) {
                        world.server?.execute {
                            player.startRiding(entity)
                            entity.networkHandler?.sendPacket(EntityPassengersSetS2CPacket(entity))
                        }
                    }
                }.whenComplete { _, _ ->
                    requests.remove(player.uuid to entity.uuid)
                    requests.remove(entity.uuid to player.uuid)
                }
            }

            ActionResult.SUCCESS
        }
    }

    override fun onServerStopping(server: MinecraftServer) = requests.forEach { it.value.complete(false) }

    private fun sendRequest(player: ServerPlayerEntity, target: ServerPlayerEntity) =
        requests.computeIfAbsent(player.uuid to target.uuid) { pair ->
            CompletableFuture<Boolean>()/*.whenComplete { _, _ -> requests.remove(it) }*/
                .completeOnTimeout(false, TIMEOUT, TimeUnit.MILLISECONDS)
                .also {
                    player.sendIfPossible(RidingRequestS2CPayload(pair.second)) { it.complete(true) }
                }
        }

    private fun isAlreadyRequested(player: PlayerEntity, target: PlayerEntity) =
        requests.containsKey(player.uuid to target.uuid)

    fun receiveResponse(packet: RidingResponseC2SPayload, player: PlayerEntity) =
        requests[player.uuid to packet.uuid]?.complete(packet.response.isAccepted)

    private fun PlayerEntity.canStartRiding(entity: Entity) =
        entity.isPlayer && id != entity.id && uuid != entity.uuid && !hasPassenger(entity) && !entity.hasPassengers()
}
