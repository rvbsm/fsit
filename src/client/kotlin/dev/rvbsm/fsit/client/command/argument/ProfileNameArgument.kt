package dev.rvbsm.fsit.client.command.argument

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import dev.rvbsm.fsit.command.argument.CommandArgument
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object ProfileNameArgument :
    CommandArgument<FabricClientCommandSource, String>("player", StringArgumentType.string()) {
    override fun suggestionsProvider(ctx: CommandContext<FabricClientCommandSource>): Iterable<String> =
        ctx.source.playerNames
}
