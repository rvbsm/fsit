package dev.rvbsm.fsit.config.migration

import com.charleskorn.kaml.YamlMap
import kotlinx.serialization.json.JsonObject

internal interface MigratedProperty {
    override fun toString(): String

    fun migrate(yamlMap: YamlMap): Boolean
    fun migrate(jsonObject: JsonObject)
}
