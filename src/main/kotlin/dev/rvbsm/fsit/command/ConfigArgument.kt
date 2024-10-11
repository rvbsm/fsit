package dev.rvbsm.fsit.command

import dev.rvbsm.fsit.FSitMod
import dev.rvbsm.fsit.util.text.literal
import net.minecraft.server.command.ServerCommandSource
import kotlin.reflect.KMutableProperty0

inline fun <reified T> CommandBuilder<ServerCommandSource, *>.configArgument(
    name: String,
    property: KMutableProperty0<T>,
) = literal(name) {
    executes {
        source.sendFeedback("Config option $name is currently set to: ${property.get()}"::literal, false)
    }

    argument<T>("value") {
        executes {
            property.set(it()).also { FSitMod.saveConfig() }
            source.sendFeedback("Config option $name is now set to: ${property.get()}"::literal, true)
        }
    }
}
