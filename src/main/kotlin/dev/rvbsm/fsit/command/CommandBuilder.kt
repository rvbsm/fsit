package dev.rvbsm.fsit.command

import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandSource
import net.minecraft.command.argument.IdentifierArgumentType
import net.minecraft.command.argument.UuidArgumentType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.Identifier
import java.util.*

val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

abstract class CommandBuilder<S, B> where S : CommandSource, B : ArgumentBuilder<S, B> {
    abstract val builder: B

    inline fun literal(name: String, literalBuilder: LiteralCommandBuilder<S>.() -> Unit = {}) =
        LiteralCommandBuilder<S>(name).apply(literalBuilder).also { builder.then(it.builder) }

    fun requires(requirement: (S) -> Boolean) = also { builder.requires(requirement) }

    inline infix fun executes(crossinline command: CommandContext<S>.() -> Unit) =
        also { builder.executes { command(it); 0 } }

    inline infix fun executesSuspend(crossinline command: suspend CommandContext<S>.() -> Unit) =
        executes { scope.launch { command() } }

    inline fun <reified T> argument(
        name: String,
        type: ArgumentType<T>,
        argumentBuilder: ArgumentCommandBuilder<S, T>.(argument: CommandContext<S>.() -> T) -> Unit,
    ) = ArgumentCommandBuilder<S, T>(name, type).apply { argumentBuilder { getArgument(name, T::class.java) } }
        .also { builder.then(it.builder) }

    inline fun <reified T> argument(
        name: String,
        argumentBuilder: ArgumentCommandBuilder<S, T>.(argument: CommandContext<S>.() -> T) -> Unit,
    ) = argument(name, getArgumentType<T>(), argumentBuilder)

    inline fun <reified T> argument(
        name: String,
        crossinline provider: CommandContext<S>.() -> Iterable<String>,
        crossinline transformer: (String) -> T,
        argumentBuilder: ArgumentCommandBuilder<S, String>.(argument: CommandContext<S>.() -> T) -> Unit,
    ) = ArgumentCommandBuilder.simple(name, provider)
        .apply { argumentBuilder { transformer(getArgument(name, String::class.java)) } }
        .also { this.builder.then(it.builder) }

    inline fun argument(
        name: String,
        crossinline provider: CommandContext<S>.() -> Iterable<String>,
        argumentBuilder: ArgumentCommandBuilder<S, String>.(argument: CommandContext<S>.() -> String) -> Unit,
    ) = argument(name, provider, { it }, argumentBuilder)
}

class LiteralCommandBuilder<S>(name: String) : CommandBuilder<S, LiteralArgumentBuilder<S>>() where S : CommandSource {
    override val builder: LiteralArgumentBuilder<S> = LiteralArgumentBuilder.literal(name)
}

class ArgumentCommandBuilder<S, T>(name: String, type: ArgumentType<T>) :
    CommandBuilder<S, RequiredArgumentBuilder<S, T>>() where S : CommandSource {

    override val builder: RequiredArgumentBuilder<S, T> = RequiredArgumentBuilder.argument(name, type)

    inline fun suggests(crossinline provider: CommandContext<S>.() -> Iterable<String>) = also {
        builder.suggests { ctx, builder -> CommandSource.suggestMatching(provider(ctx), builder) }
    }

    companion object {
        inline fun <S : CommandSource> simple(
            name: String,
            crossinline provider: CommandContext<S>.() -> Iterable<String>,
        ) = ArgumentCommandBuilder<S, String>(name, StringArgumentType.string()).suggests(provider)
    }
}

@Suppress("unchecked_cast")
inline fun <reified T> getArgumentType(): ArgumentType<T> = when (T::class) {
    Boolean::class -> BoolArgumentType.bool()
    Int::class -> IntegerArgumentType.integer()
    Long::class -> LongArgumentType.longArg()
    Float::class -> FloatArgumentType.floatArg()
    Double::class -> DoubleArgumentType.doubleArg()
    String::class -> StringArgumentType.string()
    UUID::class -> UuidArgumentType.uuid()

    Identifier::class -> IdentifierArgumentType.identifier()

    else -> throw IllegalArgumentException("rip bozo, add argument type for ${T::class.qualifiedName}")
} as ArgumentType<T>

inline fun command(name: String, builder: LiteralCommandBuilder<ServerCommandSource>.() -> Unit) =
    LiteralCommandBuilder<ServerCommandSource>(name).apply(builder).also {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(it.builder)
        }
    }
