package dev.rvbsm.fsit.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.rvbsm.fsit.FSitMod
import dev.rvbsm.fsit.command.fsit.ReloadCommand
import net.minecraft.server.command.ServerCommandSource

object MainCommand : ModCommand<ServerCommandSource> {
    override val name = "fsit"
    override val children = setOf(ReloadCommand)

    override fun builder(): LiteralArgumentBuilder<ServerCommandSource> = super.builder().apply {
        FSitMod.config.arguments.forEach { then(it.build()) }
    }
}
