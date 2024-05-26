package dev.rvbsm.fsit.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.world.World

fun interface PassedUseBlockCallback {
    fun interactBlock(player: ServerPlayerEntity, world: World, hitResult: BlockHitResult): ActionResult

    companion object {
        @JvmField
        val EVENT: Event<PassedUseBlockCallback> =
            EventFactory.createArrayBacked(PassedUseBlockCallback::class.java) { listeners ->
                PassedUseBlockCallback { player, world, hitResult ->
                    for (listener in listeners) {
                        val result = listener.interactBlock(player, world, hitResult)

                        if (result != ActionResult.PASS) {
                            return@PassedUseBlockCallback result
                        }
                    }

                    return@PassedUseBlockCallback ActionResult.PASS
                }
            }
    }
}
