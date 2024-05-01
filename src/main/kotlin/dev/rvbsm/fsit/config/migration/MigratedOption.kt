package dev.rvbsm.fsit.config.migration

import com.charleskorn.kaml.YamlMap
import kotlinx.serialization.json.JsonObject

internal interface MigratedOption {
    val key: String

    fun migrate(yamlMap: YamlMap)
    fun migrate(jsonObject: JsonObject)
}
