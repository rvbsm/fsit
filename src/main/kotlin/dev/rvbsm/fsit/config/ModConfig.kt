package dev.rvbsm.fsit.config

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.YamlNamingStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
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

private val json = Json { ignoreUnknownKeys = true; namingStrategy = JsonNamingStrategy.SnakeCase }

@Serializable
data class ModConfig(
    @Transient private val path: Path? = null,

    var useServer: Boolean = false,
    val sittable: Sittable = Sittable(),
    val riding: Riding = Riding(),
) {
    override fun toString() = toYaml()
    fun toYaml() = yaml.encodeToString(this)
    fun toJson() = json.encodeToString(this)
    internal fun write() = path?.writeText("$this")

    companion object {
        @Transient
        val default = ModConfig()

        fun fromYaml(string: String) = yaml.decodeFromString<ModConfig>(string)
        fun fromJson(string: String) = json.decodeFromString<ModConfig>(string)

        internal fun read(id: String) = configPath.resolve("$id.yaml").let {
            if (it.exists() && it.fileSize() > 0) {
                fromYaml(it.readText()).copy(path = it)
            } else {
                default.copy(path = it)
            }
        }.apply { write() }
    }

    @Serializable
    data class Sittable(
        var enabled: Boolean = true,
        var radius: Long = 2,
        var materials: MutableSet<@Serializable(Material.Serializer::class) Material> = mutableSetOf(
            BlockTags.SLABS.asMaterial(), BlockTags.STAIRS.asMaterial(), BlockTags.LOGS.asMaterial()
        )
    ) {
        init {
            require(radius in 1..4) { "sittable.radius is needed to be in 1..4" }
        }
    }

    @Serializable
    data class Riding(
        var enabled: Boolean = true,
        var radius: Long = 3,
    ) {
        init {
            require(radius in 1..4) { "riding.radius is needed to be in 1..4" }
        }
    }
}
