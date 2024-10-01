package dev.rvbsm.fsit

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.YamlNamingStrategy
import dev.rvbsm.fsit.command.command
import dev.rvbsm.fsit.command.configArgument
import dev.rvbsm.fsit.command.isGameMaster
import dev.rvbsm.fsit.config.ConfigSerializer
import dev.rvbsm.fsit.config.ModConfig
import dev.rvbsm.fsit.event.*
import dev.rvbsm.fsit.network.FSitServerNetworking
import dev.rvbsm.fsit.util.id
import dev.rvbsm.fsit.util.literal
import dev.rvbsm.fsit.util.translatable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.command.ServerCommandSource

object FSitMod : ModInitializer {
    const val MOD_ID = "fsit"

    @JvmStatic
    lateinit var config: ModConfig private set

    private val yamlConfigSerializer = ConfigSerializer.Writable(
        format = Yaml(
            configuration = YamlConfiguration(
                strictMode = false,
                yamlNamingStrategy = YamlNamingStrategy.SnakeCase,
            ),
        ), MOD_ID, "yml", "yaml"
    )

    override fun onInitialize() = runBlocking {
        loadConfig()

        FSitServerNetworking.register()

        ServerLifecycleEvents.SERVER_STOPPING.register(ServerStoppingListener)

        PassedUseEntityCallback.EVENT.register(StartRidingListener)
        PassedUseBlockCallback.EVENT.register(SpawnSeatListener)
        ClientCommandCallback.EVENT.register(ClientCommandSneakListener)
        UpdatePoseCallback.EVENT.register(UpdatePoseListener)

        registerCommands()
    }

    private fun registerCommands() {
        command(MOD_ID) {
            requires(ServerCommandSource::isGameMaster)

            literal("reload") {
                executesSuspend {
                    loadConfig()
                    source.sendFeedback("Reloaded config!"::literal, true)
                }
            }

            configArgument("useServer", config::useServer)
            configArgument("onUseSit", config.onUse::sitting)
            configArgument("onUseRide", config.onUse::riding)
            configArgument("onUseRange", config.onUse::range)
            configArgument("onUseCheckSuffocation", config.onUse::checkSuffocation)
            configArgument("onSneakSit", config.onDoubleSneak::sitting)
            configArgument("onSneakCrawl", config.onDoubleSneak::crawling)
            configArgument("onSneakMinPitch", config.onDoubleSneak::minPitch)
            configArgument("onSneakDelay", config.onDoubleSneak::delay)
        }

        arrayOf("sit" to PlayerPose.Sitting, "crawl" to PlayerPose.Crawling).forEach {
            command(it.first) {
                requires(ServerCommandSource::isExecutedByPlayer)

                executes {
                    val player = source.player!!
                    if (player.hasVehicle()) return@executes

                    if (player.isInPose()) player.resetPose()
                    else player.setPose(it.second)
                }
            }
        }
    }

    @JvmStatic
    fun id(path: String) = path.id(MOD_ID)

    @JvmStatic
    fun translatable(category: String, path: String, vararg args: Any) = "$category.$MOD_ID.$path".translatable(args)

    private suspend fun loadConfig() = coroutineScope { config = yamlConfigSerializer.read() }
    fun saveConfig() = yamlConfigSerializer.write(config)
}
