package dev.rvbsm.fsit.config

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.YamlNamingStrategy
import dev.rvbsm.fsit.config.container.BlockContainer
import dev.rvbsm.fsit.config.container.asContainer
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.registry.tag.BlockTags
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.readText
import kotlin.io.path.writeText

private val configPath = FabricLoader.getInstance().configDir

private val yamlConf = YamlConfiguration(strictMode = false, yamlNamingStrategy = YamlNamingStrategy.SnakeCase)
private val yaml = Yaml(configuration = yamlConf)

@OptIn(ExperimentalSerializationApi::class)
private val json = Json { ignoreUnknownKeys = true; namingStrategy = JsonNamingStrategy.SnakeCase }

@Serializable
data class ModConfig(
    @Transient private val path: Path? = null,

    // todo: show on the client somehow that server's `use_server` is true
    var useServer: Boolean = false,
    val sitting: Sitting = Sitting(),
    val riding: Riding = Riding(),
) {
    init {
        require(sitting.onUse.range in 1..4) { "sitting.on_use.range is needed to be in 1..4" }
        require(riding.onUse.range in 1..4) { "riding.on_use.range is needed to be in 1..4" }
        require(sitting.onDoubleSneak.minPitch in -90.0..90.0) { "sitting.on_double_sneak.min_pitch is needed to be in -90..90" }
        require(sitting.onDoubleSneak.delay in 100..2000) { "sitting.on_double_sneak.delay is needed to be in 100..2000" }
    }

    override fun toString() = toYaml()
    fun toYaml() = yaml.encodeToString(this)
    fun toJson() = json.encodeToString(this)
    internal fun write() = path?.writeText("$this")

    companion object {
        @Transient
        val default = ModConfig()

        fun fromYaml(string: String) =
            yaml.decodeFromString<ModConfig>(string).apply { migrate(yaml.parseToYamlNode(string)) }

        fun fromJson(string: String) =
            json.decodeFromString<ModConfig>(string).apply { migrate(json.parseToJsonElement(string)) }

        internal fun read(id: String) = configPath.resolve("$id.yaml").let {
            if (it.exists() && it.fileSize() > 0) {
                fromYaml(it.readText()).copy(path = it)
            } else {
                default.copy(path = it)
            }
        }.apply { write() }
    }
}

@Serializable
data class Sitting(
    var seatsGravity: Boolean = true,
    var allowMidAir: Boolean = false,
    val onUse: Use = Use(),
    val onDoubleSneak: DoubleSneak = DoubleSneak()
) {
    @Serializable
    data class Use(
        var enabled: Boolean = true,
        var range: Long = 2,
        var suffocationCheck: Boolean = true,
        val blocks: MutableSet<BlockContainer> = mutableSetOf(
            BlockTags.SLABS.asContainer(), BlockTags.STAIRS.asContainer(), BlockTags.LOGS.asContainer(),
        ),
    )

    @Serializable
    data class DoubleSneak(var enabled: Boolean = false, var minPitch: Double = 66.6, var delay: Long = 600)
}

@Serializable
data class Riding(var onUse: Use = Use()) {
    @Serializable
    data class Use(var enabled: Boolean = true, var range: Long = 3)
}
