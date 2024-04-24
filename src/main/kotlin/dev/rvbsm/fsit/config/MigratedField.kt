package dev.rvbsm.fsit.config

import com.charleskorn.kaml.YamlMap
import kotlinx.serialization.json.JsonObject

internal interface MigratedField {
    override fun toString(): String

    fun migrate(yamlMap: YamlMap): Boolean
    fun migrate(jsonObject: JsonObject)
}
