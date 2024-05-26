package dev.rvbsm.fsit.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.Entity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult

fun interface PassedUseEntityCallback {
    fun interactEntity(player: ServerPlayerEntity, world: ServerWorld, entity: Entity): ActionResult

    companion object {
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
    }
}
