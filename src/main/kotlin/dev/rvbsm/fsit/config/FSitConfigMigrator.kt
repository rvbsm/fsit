package dev.rvbsm.fsit.config

import com.charleskorn.kaml.*
import dev.rvbsm.fsit.config.container.BlockContainer
import dev.rvbsm.fsit.config.container.updateWith
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.LoggerFactory

private val migrations = setOf(
    BooleanProperty("sneak.enabled") { ModConfig::onDoubleSneak.get(it)::sitting },
    BooleanProperty("sittable.enabled") { ModConfig::onUse.get(it)::sitting },
    BooleanProperty("riding.enabled") { ModConfig::onUse.get(it)::riding },
    BooleanProperty("sitting.on_use.enabled") { ModConfig::onUse.get(it)::sitting },
    BooleanProperty("sitting.on_use.suffocation_check") { ModConfig::onUse.get(it)::checkSuffocation },
    BooleanProperty("sitting.on_double_sneak.enabled") { ModConfig::onDoubleSneak.get(it)::sitting },
    BooleanProperty("riding.on_use.enabled") { ModConfig::onUse.get(it)::riding },

    LongProperty("sneak.delay") { ModConfig::onDoubleSneak.get(it)::delay },
    LongProperty("sittable.radius") { ModConfig::onUse.get(it)::range },
    LongProperty("sitting.on_use.range") { ModConfig::onUse.get(it)::range },
    LongProperty("sitting.on_double_sneak.delay") { ModConfig::onDoubleSneak.get(it)::delay },

    DoubleProperty("sneak.angle") { ModConfig::onDoubleSneak.get(it)::minPitch },
    DoubleProperty("sitting.on_double_sneak.min_pitch") { ModConfig::onDoubleSneak.get(it)::minPitch },
)

internal object FSitConfigMigrator {
    private val logger = LoggerFactory.getLogger(FSitConfigMigrator::class.java)

    internal fun migrateYaml(config: ModConfig, yamlNode: YamlNode) {
        val yamlConfig = yamlNode.yamlMap

        migrations.forEach {
            it.config = config

            if (it.migrate(yamlConfig)) {
                logger.info("Migrated '$it'")
            }
        }

        // todo
        yamlConfig.get<YamlMap>("sittable")?.let { yamlSittable ->
            yamlSittable.get<YamlList>("blocks")?.let { yamlBlocks ->
                yamlBlocks.items.map { BlockContainer.fromString(it.yamlScalar.content) }.let {
                    config.onUse.blocks.updateWith(it)
                    logger.info("Migrated 'sittable.blocks'")
                }
            }
            yamlSittable.get<YamlList>("tags")?.let { yamlTags ->
                yamlTags.items.map { BlockContainer.fromString('#' + it.yamlScalar.content) }.let {
                    config.onUse.blocks.updateWith(it)
                    logger.info("Migrated 'sittable.tags'")
                }
            }
            yamlSittable.get<YamlList>("materials")?.let { yamlMaterials ->
                yamlMaterials.items.map { BlockContainer.fromString(it.yamlScalar.content) }.let {
                    config.onUse.blocks.updateWith(it)
                    logger.info("Migrated 'sittable.materials'")
                }
            }
        }

        yamlConfig.get<YamlMap>("sitting")?.let { yamlSitting ->
            yamlSitting.get<YamlMap>("on_use")?.let { yamlSittingOnUse ->
                yamlSittingOnUse.get<YamlList>("blocks")?.items?.map { BlockContainer.fromString(it.yamlScalar.content) }
                    ?.let {
                        logger.info("Migrated 'sitting.on_use.blocks'")
                        config.onUse.blocks.updateWith(it)
                    }
            }
        }
    }

    internal fun migrateJson(config: ModConfig, jsonElement: JsonElement) {
        val jsonConfig = jsonElement.jsonObject

        migrations.forEach {
            it.config = config

            it.migrate(jsonConfig)
        }

        jsonConfig["sittable"]?.jsonObject?.let { jsonSittable ->
            jsonSittable["tags"]?.jsonArray?.let { jsonTags ->
                jsonTags.map { BlockContainer.TagEntry.fromString('#' + it.jsonPrimitive.content) }.let {
                    config.onUse.blocks.updateWith(it)
                }
            }
            jsonSittable["materials"]?.jsonArray?.let { jsonMaterials ->
                jsonMaterials.map { BlockContainer.fromString(it.jsonPrimitive.content) }.let {
                    config.onUse.blocks.updateWith(it)
                }
            }
        }

        jsonConfig["sitting"]?.jsonObject?.let { yamlSitting ->
            yamlSitting["on_use"]?.jsonObject?.let { yamlSittingOnUse ->
                yamlSittingOnUse["blocks"]?.jsonArray?.map { BlockContainer.fromString(it.jsonPrimitive.content) }
                    ?.let {
                        config.onUse.blocks.updateWith(it)
                    }
            }
        }
    }
}

internal fun ModConfig.migrate(yamlNode: YamlNode) = FSitConfigMigrator.migrateYaml(this, yamlNode)
internal fun ModConfig.migrate(jsonElement: JsonElement) = FSitConfigMigrator.migrateJson(this, jsonElement)
