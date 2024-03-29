package dev.rvbsm.fsit.command.fsit

import com.mojang.brigadier.context.CommandContext
import dev.rvbsm.fsit.FSitMod
import dev.rvbsm.fsit.command.ModCommand
import net.minecraft.server.command.ServerCommandSource

object ReloadCommand : ModCommand<ServerCommandSource> {
    override val name = "reload"

    override fun executes(ctx: CommandContext<ServerCommandSource>): Int {
        FSitMod.loadConfig()

        return super.executes(ctx)
    }
}
