package dev.rvbsm.fsit.client.command

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import dev.rvbsm.fsit.client.command.argument.ProfileNameArgument
import dev.rvbsm.fsit.client.config.RestrictionList
import dev.rvbsm.fsit.command.ModCommand
import dev.rvbsm.fsit.command.argument.CommandArgument
import dev.rvbsm.fsit.command.argument.get
import dev.rvbsm.fsit.util.literal
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.command.argument.GameProfileArgumentType
import java.util.*

private val alreadyRestrictedException =
    SimpleCommandExceptionType("Nothing changed. The player is already restricted".literal())
private val alreadyAllowedException =
    SimpleCommandExceptionType("Nothing changed. The player isn't restricted".literal())

enum class RestrictCommand(
    inline val action: (UUID) -> Boolean, private val exceptionType: SimpleCommandExceptionType
) : ModCommand<FabricClientCommandSource> {
    Restrict(RestrictionList::add, alreadyRestrictedException), Allow(RestrictionList::remove, alreadyAllowedException);

    override val arguments: List<CommandArgument<FabricClientCommandSource, *>> = listOf(ProfileNameArgument)

    override fun executes(ctx: CommandContext<FabricClientCommandSource>): Int {
        val profileName = ProfileNameArgument.get(ctx)
        val id = ctx.source.client.networkHandler?.getPlayerListEntry(profileName)?.profile?.id
            ?: throw GameProfileArgumentType.UNKNOWN_PLAYER_EXCEPTION.create()

        if (!action(id)) {
            throw exceptionType.create()
        }

        ctx.source.sendFeedback("${name}ed $profileName".literal())
        return super.executes(ctx)
    }
}
