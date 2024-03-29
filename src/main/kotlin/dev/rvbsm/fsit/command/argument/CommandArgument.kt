package dev.rvbsm.fsit.command.argument

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource

abstract class CommandArgument<S, T>(val name: String, private val type: ArgumentType<T>) where S : CommandSource {
    abstract fun suggestionsProvider(ctx: CommandContext<S>): Iterable<String>

    fun suggestions(): RequiredArgumentBuilder<S, T> = RequiredArgumentBuilder.argument<S, T>(name, type)
        .suggests { ctx, builder -> CommandSource.suggestMatching(suggestionsProvider(ctx), builder) }
}

inline operator fun <S, reified T> CommandArgument<S, T>.get(ctx: CommandContext<S>): T where S : CommandSource =
    ctx.getArgument(name, T::class.java)
