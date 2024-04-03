package dev.rvbsm.fsit.command

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.context.CommandContext
import dev.rvbsm.fsit.FSitMod
import dev.rvbsm.fsit.util.literal
import net.minecraft.server.command.ServerCommandSource
import kotlin.reflect.KMutableProperty

enum class ConfigCommand(
    private val property: KMutableProperty<*>,
    private val type: ArgumentType<*>,
    private val clazz: Class<*>,
) : ModCommand<ServerCommandSource> {
    UseServer(FSitMod.config::useServer, BoolArgumentType.bool(), Boolean::class.java),
    SittableEnabled(FSitMod.config.sittable::enabled, BoolArgumentType.bool(), Boolean::class.java),
    SittableRadius(FSitMod.config.sittable::radius, LongArgumentType.longArg(1, 4), Long::class.java),
    RidingEnabled(FSitMod.config.riding::enabled, BoolArgumentType.bool(), Boolean::class.java),
    RidingRadius(FSitMod.config.riding::radius, LongArgumentType.longArg(1, 4), Long::class.java);

    override fun requires(src: ServerCommandSource) = src.hasPermissionLevel(2)

    @Suppress("unchecked_cast")
    override fun builder(): LiteralArgumentBuilder<ServerCommandSource> = super.builder().then(
        argument<ServerCommandSource, Any>("value", type as ArgumentType<Any>).executes(::executeSet)
    )

    override fun executes(ctx: CommandContext<ServerCommandSource>): Int {
        ctx.source.sendFeedback("Config option $name is currently set to: ${get()}"::literal, false)

        return super.executes(ctx)
    }

    private fun executeSet(ctx: CommandContext<ServerCommandSource>): Int {
        val value = ctx.getArgument("value", clazz).also { set(it) }
        ctx.source.sendFeedback("Config option $name is now set to: $value"::literal, true)

        return super.executes(ctx)
    }

    fun set(value: Any) = property.setter.call(value).also { FSitMod.saveConfig() }
    fun get() = property.getter.call()
}
