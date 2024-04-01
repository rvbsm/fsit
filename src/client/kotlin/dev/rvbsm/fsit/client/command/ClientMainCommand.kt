package dev.rvbsm.fsit.client.command

import dev.rvbsm.fsit.command.ModCommand
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object ClientMainCommand : ModCommand<FabricClientCommandSource> {
    override val name = "fsit"
    override val children = setOf(*enumValues<RestrictCommand>())
}