package dev.rvbsm.fsit.api.event;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PassedUseBlockCallback {

    Event<PassedUseBlockCallback> EVENT = EventFactory.createArrayBacked(
        PassedUseBlockCallback.class, (listeners) -> (player, world, hitResult) -> {
            for (PassedUseBlockCallback listener : listeners) {
                final ActionResult result = listener.interact(player, world, hitResult);

                if (result != ActionResult.PASS) {
                    return result;
                }
            }

            return ActionResult.PASS;
        });

    ActionResult interact(@NotNull ServerPlayerEntity player, @NotNull World world, @NotNull BlockHitResult hitResult);
}
