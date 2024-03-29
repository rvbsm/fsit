package dev.rvbsm.fsit.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import dev.rvbsm.fsit.command.argument.CommandArgument
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.CommandSource
import net.minecraft.server.command.CommandManager.RegistrationEnvironment

interface ModCommand<S> where S : CommandSource {
    val name: String
    val arguments get() = listOf<CommandArgument<S, *>>()
    val children get() = setOf<ModCommand<S>>()

    fun register(
        dispatcher: CommandDispatcher<S>, registryAccess: CommandRegistryAccess, environment: RegistrationEnvironment
    ): LiteralCommandNode<S> = dispatcher.register(builder())

    @Environment(EnvType.CLIENT)
    fun register(dispatcher: CommandDispatcher<S>, registryAccess: CommandRegistryAccess): LiteralCommandNode<S> =
        dispatcher.register(builder())

    fun builder(): LiteralArgumentBuilder<S> {
        val builder = LiteralArgumentBuilder.literal<S>(name.lowercase()).requires(::requires)
        children.forEach { builder.then(it.builder()) }

        return arguments.foldRight<CommandArgument<S, *>, RequiredArgumentBuilder<S, *>?>(null) { argument, acc ->
            acc?.let { argument.suggestions().then(it) } ?: argument.suggestions().executes(::executes)
        }?.let { builder.then(it) } ?: builder.executes(::executes)
    }

    fun requires(src: S) = true

    fun executes(ctx: CommandContext<S>) = Command.SINGLE_SUCCESS
}
