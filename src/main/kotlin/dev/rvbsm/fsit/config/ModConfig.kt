package dev.rvbsm.fsit.config

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlComment
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.YamlNamingStrategy
import dev.rvbsm.fsit.config.serialization.RegistrySetSerializer
import dev.rvbsm.fsit.util.RegistrySet
import dev.rvbsm.fsit.util.registrySetOf
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.Block
import net.minecraft.registry.tag.BlockTags
import java.nio.file.Path
import kotlin.io.path.*

private val configDirPath = FabricLoader.getInstance().configDir

private val yamlConf = YamlConfiguration(strictMode = false, yamlNamingStrategy = YamlNamingStrategy.SnakeCase)
private val yaml = Yaml(configuration = yamlConf)

@OptIn(ExperimentalSerializationApi::class)
private val json = Json { ignoreUnknownKeys = true; namingStrategy = JsonNamingStrategy.SnakeCase }

@Serializable
data class ModConfig(
    @Transient private val path: Path? = null,

    // todo: show on the client somehow that server's `use_server` is true
    @YamlComment("Whether to use the server-side configuration.")
    var useServer: Boolean = false,
    val sitting: Sitting = Sitting(),
    val riding: Riding = Riding(),
    val onUse: OnUse = OnUse(),
    val onDoubleSneak: OnDoubleSneak = OnDoubleSneak()
) {
    init {
        require(onUse.range in 1..4) { "sitting.on_use.range is needed to be in 1..4" }
        require(onDoubleSneak.minPitch in -90.0..90.0) { "sitting.on_double_sneak.min_pitch is needed to be in -90..90" }
        require(onDoubleSneak.delay in 100..2000) { "sitting.on_double_sneak.delay is needed to be in 100..2000" }
    }

    override fun toString() = encodeYaml()
    fun encodeYaml() = yaml.encodeToString(this)
    fun encodeJson() = json.encodeToString(this)
    internal fun write() = path?.writeText("$this")

    companion object {
        @Transient
        val default = ModConfig()

        fun decodeYaml(string: String) =
            yaml.decodeFromString<ModConfig>(string).apply { migrate(yaml.parseToYamlNode(string)) }

        fun decodeJson(string: String) =
            json.decodeFromString<ModConfig>(string).apply { migrate(json.parseToJsonElement(string)) }

        internal fun read(id: String): ModConfig {
            val ymlConfigPath = configDirPath.resolve("$id.yml")
            val yamlConfigPath = configDirPath.resolve("$id.yaml")

            return when {
                ymlConfigPath.exists() && ymlConfigPath.fileSize() > 0 -> decodeYaml(ymlConfigPath.readText())
                yamlConfigPath.exists() && yamlConfigPath.fileSize() > 0 -> {
                    decodeYaml(yamlConfigPath.readText()).also {
                        yamlConfigPath.deleteExisting()
                    }
                }

                else -> default
            }.copy(path = ymlConfigPath).apply { write() }
        }
    }
}

@Serializable
data class Sitting(
    @YamlComment("Controls whether gravity affects seats.")
    var applyGravity: Boolean = true,
    @YamlComment("Allows sitting even if not standing on a solid block.")
    var allowInAir: Boolean = false,
)

@Serializable
data class Riding(
    @YamlComment("Whether to hide a player's rider when the player is not looking at him.")
    var hideRider: Boolean = true,
)

@Serializable
data class OnUse(
    @YamlComment("Allows to start sitting on specific blocks by interacting with them.")
    var sitting: Boolean = true,
    @YamlComment("Allows to start riding other players by interaction with them.")
    var riding: Boolean = true,

    @YamlComment("The maximum distance to a target to interact.")
    var range: Long = 2,
    @YamlComment("Prevents players from sitting in places where they would suffocate.")
    var checkSuffocation: Boolean = true,
    @Serializable(RegistrySetSerializer.Block::class)
    @YamlComment("List of blocks or block types (e.g., \"oak_log\", \"#logs\") that are available to sit on by interacting with them.")
    var blocks: RegistrySet<@Contextual Block> = registrySetOf(BlockTags.SLABS, BlockTags.STAIRS, BlockTags.LOGS)
)

@Serializable
data class OnDoubleSneak(
    @YamlComment("Allows to start sitting by double sneaking while looking down.")
    var sitting: Boolean = true,
    @YamlComment("Allows to start crawling by double sneaking near a one-block gap.")
    var crawling: Boolean = true,

    @YamlComment("The minimum angle must be looking down (in degrees) with double sneak.")
    var minPitch: Double = 66.6,
    @YamlComment("The window between sneaks to sit down (in milliseconds).")
    var delay: Long = 600,
)
