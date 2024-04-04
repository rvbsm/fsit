package dev.rvbsm.fsit.command

import com.mojang.brigadier.context.CommandContext
import dev.rvbsm.fsit.FSitMod
import dev.rvbsm.fsit.util.literal
import net.minecraft.server.command.ServerCommandSource

object ReloadCommand : ModCommand<ServerCommandSource> {
    override val name = "reload"

    override fun executes(ctx: CommandContext<ServerCommandSource>): Int {
        FSitMod.loadConfig()

        ctx.source.sendFeedback("Reloading config!"::literal, true)
        return super.executes(ctx)
    }
}
