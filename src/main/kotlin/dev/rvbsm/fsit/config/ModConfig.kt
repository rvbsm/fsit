package dev.rvbsm.fsit.config

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.YamlNamingStrategy
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.LongArgumentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
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

// todo: migration from TOML
@Serializable
data class ModConfig(
    @Transient private val path: Path = configPath, // hm

    var useServer: Boolean = false,
    val sittable: Sittable = Sittable(),
    val riding: Riding = Riding(),
) {
    @Transient
    val arguments: Set<ConfigCommandArgument<*>> = setOf(
        ConfigCommandArgument.of("useServer", ::useServer, BoolArgumentType.bool()),
        ConfigCommandArgument.of("sittableEnabled", sittable::enabled, BoolArgumentType.bool()),
        ConfigCommandArgument.of("sittableRadius", sittable::radius, LongArgumentType.longArg(0, 4)),
        ConfigCommandArgument.of("ridingEnabled", riding::enabled, BoolArgumentType.bool()),
        ConfigCommandArgument.of("ridingRadius", riding::radius, LongArgumentType.longArg(0, 4)),
    )

    override fun toString() = yaml.encodeToString(this)
    internal fun write() = path.writeText("$this")

    companion object {
        @Transient
        val default = ModConfig()

        private fun fromString(string: String) = yaml.decodeFromString<ModConfig>(string)

        internal fun read(id: String) = configPath.resolve("$id.yaml").let {
            if (it.exists() && it.fileSize() > 0) {
                fromString(it.readText()).copy(path = it)
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
            require(radius in 0..4) { "sittable.radius is needed to be in 0..4" }
        }
    }

    @Serializable
    data class Riding(
        var enabled: Boolean = true,
        var radius: Long = 3,
    ) {
        init {
            require(radius in 0..4) { "riding.radius is needed to be in 0..4" }
        }
    }
}
