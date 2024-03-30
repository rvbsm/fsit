package dev.rvbsm.fsit.config

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import dev.rvbsm.fsit.FSitMod
import dev.rvbsm.fsit.util.literal
import net.minecraft.server.command.ServerCommandSource
import kotlin.reflect.KMutableProperty

data class ConfigCommandArgument<T>(
    val path: String,
    val property: KMutableProperty<T>,
    val type: ArgumentType<T>,
    val clazz: Class<T>,
) {
    fun set(value: T) = property.setter.call(value).also { FSitMod.saveConfig() }
    fun get() = property.getter.call()

    fun build(): LiteralArgumentBuilder<ServerCommandSource> =
        LiteralArgumentBuilder.literal<ServerCommandSource>(path).executes {
            it.source.sendFeedback("Config option $path is currently set to: ${get()}"::literal, false)
            Command.SINGLE_SUCCESS
        }.then(RequiredArgumentBuilder.argument<ServerCommandSource, T>("value", type).executes {
            val value = it.getArgument("value", clazz)
            set(value)

            it.source.sendFeedback("Config option $path is now set to: $value"::literal, true)
            Command.SINGLE_SUCCESS
        })

    companion object {
        inline fun <reified T> of(path: String, property: KMutableProperty<T>, type: ArgumentType<T>) =
            ConfigCommandArgument(path, property, type, T::class.java)
    }
}
