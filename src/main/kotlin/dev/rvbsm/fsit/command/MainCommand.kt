package dev.rvbsm.fsit.command

import net.minecraft.server.command.ServerCommandSource

object MainCommand : ModCommand<ServerCommandSource> {
    override val name = "fsit"
    override val children = setOf(ReloadCommand, *enumValues<ConfigCommand>())
}
