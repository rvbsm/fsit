package dev.rvbsm.fsit.command

import dev.rvbsm.fsit.command.fsit.ConfigCommand
import dev.rvbsm.fsit.command.fsit.ReloadCommand
import net.minecraft.server.command.ServerCommandSource

object MainCommand : ModCommand<ServerCommandSource> {
    override val name = "fsit"
    override val children = setOf(ReloadCommand, *enumValues<ConfigCommand>())
}
