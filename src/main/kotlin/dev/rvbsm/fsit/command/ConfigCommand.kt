package dev.rvbsm.fsit.command

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType
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
    SeatsGravity(FSitMod.config.sitting::seatsGravity, BoolArgumentType.bool(), Boolean::class.java),
    SitOnUse(FSitMod.config.sitting.onUse::enabled, BoolArgumentType.bool(), Boolean::class.java),
    SitOnUseRange(FSitMod.config.sitting.onUse::range, LongArgumentType.longArg(1, 4), Long::class.java),
    SitOnSneak(FSitMod.config.sitting.onDoubleSneak::enabled, BoolArgumentType.bool(), Boolean::class.java),
    SitOnSneakMinPitch(FSitMod.config.sitting.onDoubleSneak::minPitch, DoubleArgumentType.doubleArg(-90.0, 90.0), Double::class.java),
    SitOnSneakDelay(FSitMod.config.sitting.onDoubleSneak::delay, LongArgumentType.longArg(100, 2000), Long::class.java),
    RideOnUse(FSitMod.config.riding.onUse::enabled, BoolArgumentType.bool(), Boolean::class.java),
    RideOnUseRange(FSitMod.config.riding.onUse::range, LongArgumentType.longArg(1, 4), Long::class.java);

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

    private fun set(value: Any) = property.setter.call(value).also { FSitMod.saveConfig() }
    private fun get() = property.getter.call()
}
