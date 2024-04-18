package dev.rvbsm.fsit.command

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.context.CommandContext
import dev.rvbsm.fsit.FSitMod
import dev.rvbsm.fsit.config.ModConfig
import dev.rvbsm.fsit.config.MutablePropertyProvider
import dev.rvbsm.fsit.util.literal
import net.minecraft.server.command.ServerCommandSource

enum class ConfigCommand(
    private val propertyProvider: MutablePropertyProvider<*>,
    private val type: ArgumentType<*>,
    private val clazz: Class<*>,
) : ModCommand<ServerCommandSource> {
    UseServer({ it::useServer }, BoolArgumentType.bool(), Boolean::class.java),
    SeatsGravity({ ModConfig::sitting.get(it)::seatsGravity }, BoolArgumentType.bool(), Boolean::class.java),
    AllowSittingInAir({ ModConfig::sitting.get(it)::allowMidAir }, BoolArgumentType.bool(), Boolean::class.java),
    OnUseSit({ ModConfig::onUse.get(it)::sitting }, BoolArgumentType.bool(), Boolean::class.java),
    OnUseRide({ ModConfig::onUse.get(it)::riding }, BoolArgumentType.bool(), Boolean::class.java),
    OnUseRange({ ModConfig::onUse.get(it)::range }, LongArgumentType.longArg(1, 4), Long::class.java),
    OnUseSuffocationCheck({ ModConfig::onUse.get(it)::suffocationCheck }, BoolArgumentType.bool(), Boolean::class.java),
    OnSneakSit({ ModConfig::onDoubleSneak.get(it)::sitting }, BoolArgumentType.bool(), Boolean::class.java),
    OnSneakCrawl({ ModConfig::onDoubleSneak.get(it)::crawling }, BoolArgumentType.bool(), Boolean::class.java),
    OnSneakMinPitch({ ModConfig::onDoubleSneak.get(it)::minPitch }, DoubleArgumentType.doubleArg(-90.0, 90.0), Double::class.java),
    OnSneakDelay({ ModConfig::onDoubleSneak.get(it)::delay }, LongArgumentType.longArg(100, 2000), Long::class.java),
    ;

    private val property get() = propertyProvider(FSitMod.config)

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
