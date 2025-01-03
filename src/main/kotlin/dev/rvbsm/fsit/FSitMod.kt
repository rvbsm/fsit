package dev.rvbsm.fsit

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlNamingStrategy
import dev.rvbsm.fsit.api.event.ClientCommandCallback
import dev.rvbsm.fsit.api.event.PassedUseBlockCallback
import dev.rvbsm.fsit.api.event.PassedUseEntityCallback
import dev.rvbsm.fsit.api.event.UpdatePoseCallback
import dev.rvbsm.fsit.command.CommandBuilder
import dev.rvbsm.fsit.command.command
import dev.rvbsm.fsit.command.isGameMaster
import dev.rvbsm.fsit.config.ModConfig
import dev.rvbsm.fsit.config.configSchemas
import dev.rvbsm.fsit.config.orDefault
import dev.rvbsm.fsit.entity.ModPose
import dev.rvbsm.fsit.event.RidingRequestListener
import dev.rvbsm.fsit.event.SneakListener
import dev.rvbsm.fsit.event.SpawnSeatListener
import dev.rvbsm.fsit.event.UpdatePoseListener
import dev.rvbsm.fsit.networking.ConfigUpdateC2SHandler
import dev.rvbsm.fsit.networking.PoseRequestC2SHandler
import dev.rvbsm.fsit.networking.RidingResponseC2SHandler
import dev.rvbsm.fsit.networking.isInPose
import dev.rvbsm.fsit.networking.modPose
import dev.rvbsm.fsit.networking.payload.ConfigUpdateC2SPayload
import dev.rvbsm.fsit.networking.payload.PoseRequestC2SPayload
import dev.rvbsm.fsit.networking.payload.RidingResponseC2SPayload
import dev.rvbsm.fsit.networking.resetPose
import dev.rvbsm.fsit.serialization.UUIDSerializer
import dev.rvbsm.fsit.serialization.Yaml
import dev.rvbsm.fsit.serialization.asReader
import dev.rvbsm.fsit.serialization.asSerializer
import dev.rvbsm.fsit.serialization.migration.version
import dev.rvbsm.fsit.serialization.migration.withMigration
import dev.rvbsm.fsit.serialization.withDefault
import dev.rvbsm.fsit.util.id
import dev.rvbsm.fsit.util.text.literal
import dev.rvbsm.fsit.util.text.translatable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.serializersModuleOf
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.ServerCommandSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal lateinit var minecraftServer: MinecraftServer

@PublishedApi
internal val modScope: CoroutineScope by lazy {
    CoroutineScope(minecraftServer.asCoroutineDispatcher() + SupervisorJob())
}

@PublishedApi
internal val modLogger: Logger = LoggerFactory.getLogger(FSitMod::class.java)

@OptIn(ExperimentalSerializationApi::class)
@Suppress("json_format_redundant")
val jsonSerializer = Json {
    ignoreUnknownKeys = true
    namingStrategy = JsonNamingStrategy.SnakeCase

    serializersModule += serializersModuleOf(UUIDSerializer)

    withMigration<ModConfig>(configSchemas) { if ("config_version" in this) -1 else version }
    withDefault(::ModConfig)
}.asSerializer()

val yamlSerializer = Yaml {
    strictMode = false
    yamlNamingStrategy = YamlNamingStrategy.SnakeCase

    serializersModule += serializersModuleOf(UUIDSerializer)

    withMigration<ModConfig>(configSchemas)
    withDefault(::ModConfig)
}.asSerializer()

object FSitMod : ModInitializer {
    const val MOD_ID = "fsit"

    private val configReader = yamlSerializer.asReader<Yaml, ModConfig>(
        FabricLoader.getInstance().configDir, MOD_ID, listOf("yml", "yaml"), writeToFile = true,
        ModConfig::Default,
    )

    @JvmStatic
    var config: ModConfig = ModConfig.Default
        private set

    suspend fun writeConfig(newConfig: ModConfig) {
        config = newConfig
        configReader.write(newConfig)
    }

    @JvmStatic
    fun id(path: String) = path.id(MOD_ID)

    @JvmStatic
    fun translatable(category: String, path: String, vararg args: Any) = "$category.$MOD_ID.$path".translatable(args)

    override fun onInitialize() = runBlocking {
        config = configReader.read().orDefault()

        registerPayloads()
        registerEvents()
        registerCommands()
    }

    private fun registerPayloads() {
        ServerPlayNetworking.registerGlobalReceiver(ConfigUpdateC2SPayload.packetId, ConfigUpdateC2SHandler)
        ServerPlayNetworking.registerGlobalReceiver(PoseRequestC2SPayload.packetId, PoseRequestC2SHandler)
        ServerPlayNetworking.registerGlobalReceiver(RidingResponseC2SPayload.packetId, RidingResponseC2SHandler)
    }

    private fun registerEvents() {
        ServerLifecycleEvents.SERVER_STARTING.register { minecraftServer = it }

        PassedUseEntityCallback.EVENT.register(RidingRequestListener)
        PassedUseBlockCallback.EVENT.register(SpawnSeatListener)
        ClientCommandCallback.EVENT.register(SneakListener)
        UpdatePoseCallback.EVENT.register(UpdatePoseListener)
    }

    private fun registerCommands() {
        command(MOD_ID) {
            requires(ServerCommandSource::isGameMaster)

            literal("reload") executesSuspend {
                config = configReader.read().orDefault()
                source.sendFeedback("Reloaded config!"::literal, true)
            }

            configArgument("useServer", { config.useServer }) { useServer = it }
            configArgument("centerSeats", { config.sitting.shouldCenter }) { sittingShouldCenter = it }
            configArgument("onUseSit", { config.onUse.sitting }) { onUseSitting = it }
            configArgument("onUseRide", { config.onUse.riding }) { onUseRiding = it }
            configArgument("onUseRange", { config.onUse.range }) { onUseRange = it }
            configArgument("onUseCheckSuffocation", { config.onUse.checkSuffocation }) { onUseCheckSuffocation = it }
            configArgument("onSneakSit", { config.onSneak.sitting }) { onSneakSitting = it }
            configArgument("onSneakCrawl", { config.onSneak.crawling }) { onSneakCrawling = it }
            configArgument("onSneakMinPitch", { config.onSneak.minPitch }) { onSneakMinPitch = it }
            configArgument("onSneakDelay", { config.onSneak.delay }) { onSneakDelay = it }
        }

        fun poseCommand(name: String, pose: ModPose) = command(name) {
            requires(ServerCommandSource::isExecutedByPlayer)

            executes {
                val player = source.player!!
                if (player.hasVehicle()) return@executes

                if (player.isInPose()) player.resetPose()
                else player.modPose = pose
            }
        }

        poseCommand("sit", ModPose.Sitting)
        poseCommand("crawl", ModPose.Crawling)
    }
}

private inline fun <reified T> CommandBuilder<ServerCommandSource, *>.configArgument(
    name: String,
    crossinline getValue: () -> T,
    crossinline setValue: ModConfig.Builder.(T) -> Unit,
) = literal(name) {
    executes {
        val value = getValue()
        source.sendFeedback("Config option $name is currently set to: $value"::literal, false)
    }

    argument<T>("value") {
        executesSuspend {
            val configBuilder = ModConfig.Builder(FSitMod.config)
            configBuilder.setValue(it())
            FSitMod.writeConfig(configBuilder.build())

            val value = getValue()
            source.sendFeedback("Config option $name is now set to: $value"::literal, true)
        }
    }
}
