package dev.rvbsm.fsit.api.event;

import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ClientCommandCallback {

    Event<ClientCommandCallback> EVENT = EventFactory.createArrayBacked(
        ClientCommandCallback.class, (listeners) -> (player, mode) -> {
            for (ClientCommandCallback listener : listeners) {
                listener.process(player, mode);
            }
        });

    void process(@NotNull ServerPlayerEntity player, @NotNull ClientCommandC2SPacket.Mode mode);
}
