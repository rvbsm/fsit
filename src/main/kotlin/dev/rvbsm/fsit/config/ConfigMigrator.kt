package dev.rvbsm.fsit.config

import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.yamlMap
import dev.rvbsm.fsit.config.migration.BooleanProperty
import dev.rvbsm.fsit.config.migration.DoubleProperty
import dev.rvbsm.fsit.config.migration.LongProperty
import dev.rvbsm.fsit.config.migration.RegistrySetProperty
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

private val migrations = setOf(
    BooleanProperty("sneak.enabled") { ModConfig::onDoubleSneak.get(it)::sitting },
    BooleanProperty("sittable.enabled") { ModConfig::onUse.get(it)::sitting },
    BooleanProperty("riding.enabled") { ModConfig::onUse.get(it)::riding },
    BooleanProperty("sitting.on_use.enabled") { ModConfig::onUse.get(it)::sitting },
    BooleanProperty("sitting.on_use.suffocation_check") { ModConfig::onUse.get(it)::checkSuffocation },
    BooleanProperty("sitting.on_double_sneak.enabled") { ModConfig::onDoubleSneak.get(it)::sitting },
    BooleanProperty("riding.on_use.enabled") { ModConfig::onUse.get(it)::riding },
    BooleanProperty("sitting.seats_gravity") { ModConfig::sitting.get(it)::applyGravity },

    LongProperty("sneak.delay") { ModConfig::onDoubleSneak.get(it)::delay },
    LongProperty("sittable.radius") { ModConfig::onUse.get(it)::range },
    LongProperty("sitting.on_use.range") { ModConfig::onUse.get(it)::range },
    LongProperty("sitting.on_double_sneak.delay") { ModConfig::onDoubleSneak.get(it)::delay },

    DoubleProperty("sneak.angle") { ModConfig::onDoubleSneak.get(it)::minPitch },
    DoubleProperty("sitting.on_double_sneak.min_pitch") { ModConfig::onDoubleSneak.get(it)::minPitch },

    RegistrySetProperty("sittable.blocks", { ModConfig::onUse.get(it)::blocks }, RegistrySetProperty.Type.ENTRIES),
    RegistrySetProperty("sittable.tags", { ModConfig::onUse.get(it)::blocks }, RegistrySetProperty.Type.TAGS, false),
    RegistrySetProperty("sittable.materials", { ModConfig::onUse.get(it)::blocks }, RegistrySetProperty.Type.CONTAINER),
    RegistrySetProperty("sitting.blocks", { ModConfig::onUse.get(it)::blocks }, RegistrySetProperty.Type.CONTAINER),
    RegistrySetProperty("sitting.on_use.blocks", { ModConfig::onUse.get(it)::blocks }, RegistrySetProperty.Type.CONTAINER),
)

internal object ConfigMigrator {
    internal fun migrateYaml(config: ModConfig, yamlNode: YamlNode) {
        val yamlConfig = yamlNode.yamlMap

        migrations.forEach {
            it.config = config

            it.migrate(yamlConfig)
        }
    }

    internal fun migrateJson(config: ModConfig, jsonElement: JsonElement) {
        val jsonConfig = jsonElement.jsonObject

        migrations.forEach {
            it.config = config

            it.migrate(jsonConfig)
        }
    }
}

internal fun ModConfig.migrate(yamlNode: YamlNode) = ConfigMigrator.migrateYaml(this, yamlNode)
internal fun ModConfig.migrate(jsonElement: JsonElement) = ConfigMigrator.migrateJson(this, jsonElement)
