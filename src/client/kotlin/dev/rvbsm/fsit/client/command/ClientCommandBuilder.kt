package dev.rvbsm.fsit.client.command

import dev.rvbsm.fsit.command.LiteralCommandBuilder
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

inline fun command(name: String, builder: LiteralCommandBuilder<FabricClientCommandSource>.() -> Unit) =
    LiteralCommandBuilder<FabricClientCommandSource>(name).apply(builder).also {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(it.builder)
        }
    }
