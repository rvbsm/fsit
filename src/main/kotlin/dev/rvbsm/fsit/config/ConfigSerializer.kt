package dev.rvbsm.fsit.config

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.yamlMap
import dev.rvbsm.fsit.util.decodeFromYamlNode
import kotlinx.serialization.StringFormat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path
import kotlin.io.path.*

internal val configDirPath = FabricLoader.getInstance().configDir

internal open class ConfigSerializer(private val format: StringFormat) : StringFormat by format {
    fun encode(config: ModConfig): String = encodeToString(config)
    fun decode(string: String): ModConfig = when (format) {
        is Json -> format.decodeFromJsonElement(format.parseToJsonElement(string).jsonObject.let { it.migrate(it.migrations) })
        is Yaml -> format.decodeFromYamlNode(format.parseToYamlNode(string).yamlMap.let { it.migrate(it.migrations) })

        else -> decodeFromString(string)
    }

    internal class Writable(
        format: StringFormat,
        private val id: String,
        private vararg val fileExtensions: String,
    ) : ConfigSerializer(format) {

        private val defaultPath = configDirPath.resolve("$id.${fileExtensions.first()}")
        private val configPath =
            configDirPath.find { it.name == id && fileExtensions.any(it.extension::equals) } ?: defaultPath

        private fun readOrDefault(path: Path): ModConfig = if (path.fileSize() > 0) decode(path.readText())
        else ModConfig()

        internal fun write(config: ModConfig) = config.path?.writeText(this.encode(config)) ?: Unit
        internal fun read(): ModConfig = readOrDefault(configPath).copy(path = defaultPath).also { write(it) }
    }
}
