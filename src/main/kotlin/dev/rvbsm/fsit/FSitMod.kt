package dev.rvbsm.fsit

import dev.rvbsm.fsit.command.MainCommand
import dev.rvbsm.fsit.command.PoseCommand
import dev.rvbsm.fsit.config.ModConfig
import dev.rvbsm.fsit.event.*
import dev.rvbsm.fsit.network.FSitServerNetworking
import dev.rvbsm.fsit.util.id
import dev.rvbsm.fsit.util.translatable
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import org.slf4j.LoggerFactory

object FSitMod : ModInitializer {
    const val MOD_ID = "fsit"
    private val logger = LoggerFactory.getLogger(FSitMod::class.java)

    @JvmStatic
    lateinit var config: ModConfig
        private set

    override fun onInitialize() {
        loadConfig()

        FSitServerNetworking.register()

        UseEntityCallback.EVENT.register(UseEntityListener)
        ServerLifecycleEvents.SERVER_STOPPING.register(ServerLifecycleListener)

        PassedUseBlockCallback.EVENT.register(PassedUseBlockCallback)
        ClientCommandCallback.EVENT.register(ClientCommandCallback)
        UpdatePoseCallback.EVENT.register(UpdatePoseCallback)

        CommandRegistrationCallback.EVENT.register(MainCommand::register)
        enumValues<PoseCommand>().forEach {
            CommandRegistrationCallback.EVENT.register(it::register)
        }
    }

    @JvmStatic
    fun id(path: String) = path.id(MOD_ID)

    @JvmStatic
    fun translatable(category: String, path: String, vararg args: Any) = "$category.$MOD_ID.$path".translatable(args)

    fun loadConfig() = run { config = ModConfig.read(MOD_ID) }
    fun saveConfig() = config.write()
}
