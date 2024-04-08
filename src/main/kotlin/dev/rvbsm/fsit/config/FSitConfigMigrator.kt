package dev.rvbsm.fsit.config

import com.charleskorn.kaml.*
import dev.rvbsm.fsit.config.container.BlockContainer
import dev.rvbsm.fsit.config.container.updateWith
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory

// todo
internal object FSitConfigMigrator {
    private val logger = LoggerFactory.getLogger(FSitConfigMigrator::class.java)

    internal fun fromYaml(config: ModConfig, yamlNode: YamlNode) {
        val yamlConfig = yamlNode.yamlMap

        yamlConfig.get<YamlMap>("sittable")?.let { yamlSittable ->
            yamlSittable.get<YamlScalar>("enabled")?.let { config.sitting.onUse.enabled = it.toBoolean() }?.let {
                logger.info("Migrated 'sittable.enabled' to 'sitting.on_use.enabled'")
            }
            yamlSittable.get<YamlScalar>("radius")?.let { config.sitting.onUse.range = it.toLong() }?.let {
                logger.info("Migrated 'sittable.radius' to 'sitting.on_use.range'")
            }
            yamlSittable.get<YamlList>("blocks")?.let { yamlBlocks ->
                yamlBlocks.items.map { BlockContainer.fromString(it.yamlScalar.content) }.let {
                    logger.info("Migrated 'sittable.blocks' to 'sitting.on_use.blocks'")
                    config.sitting.onUse.blocks.updateWith(it)
                }
            }
            yamlSittable.get<YamlList>("tags")?.let { yamlTags ->
                yamlTags.items.map { BlockContainer.fromString('#' + it.yamlScalar.content) }.let {
                    logger.info("Migrated 'sittable.tags' to 'sitting.on_use.blocks'")
                    config.sitting.onUse.blocks.updateWith(it)
                }
            }
            yamlSittable.get<YamlList>("materials")?.let { yamlMaterials ->
                yamlMaterials.items.map { BlockContainer.fromString(it.yamlScalar.content) }.let {
                    logger.info("Migrated 'sittable.materials' to 'sitting.on_use.blocks'")
                    config.sitting.onUse.blocks.updateWith(it)
                }
            }
        }.let { logger.info("Migrated 'sittable' to 'sitting'") }

        yamlConfig.get<YamlMap>("riding")?.let { yamlRiding ->
            yamlRiding.get<YamlScalar>("enabled")?.let { config.riding.onUse.enabled = it.toBoolean() }?.let {
                logger.info("Migrated 'sittable.enabled' to 'sitting.on_use.enabled'")
            }
            yamlRiding.get<YamlScalar>("radius")?.let { config.riding.onUse.range = it.toLong() }?.let {
                logger.info("Migrated 'sittable.radius' to 'sitting.on_use.range'")
            }
        }
    }

    internal fun fromJson(config: ModConfig, jsonElement: JsonElement) {
        val jsonObject = jsonElement.jsonObject

        jsonObject["sittable"]?.jsonObject?.let { jsonSittable ->
            jsonSittable["enabled"]?.jsonPrimitive?.let { config.sitting.onUse.enabled = it.boolean }
            jsonSittable["radius"]?.jsonPrimitive?.let { config.sitting.onUse.range = it.long }
            jsonSittable["blocks"]?.jsonArray?.let { jsonBlocks ->
                jsonBlocks.map { BlockContainer.BlockEntry.fromString(it.jsonPrimitive.content) }.let {
                    config.sitting.onUse.blocks.updateWith(it)
                }
            }
            jsonSittable["tags"]?.jsonArray?.let { jsonTags ->
                jsonTags.map { BlockContainer.TagEntry.fromString('#' + it.jsonPrimitive.content) }.let {
                    config.sitting.onUse.blocks.updateWith(it)
                }
            }
            jsonSittable["materials"]?.jsonArray?.let { jsonMaterials ->
                jsonMaterials.map { BlockContainer.fromString(it.jsonPrimitive.content) }.let {
                    config.sitting.onUse.blocks.updateWith(it)
                }
            }
        }

        jsonObject["riding"]?.jsonObject?.let { jsonRiding ->
            jsonRiding["enabled"]?.jsonPrimitive?.let { config.riding.onUse.enabled = it.boolean }
            jsonRiding["radius"]?.jsonPrimitive?.let { config.riding.onUse.range = it.long }
        }

        jsonObject["sneak"]?.jsonObject?.let { jsonSneak ->
            jsonSneak["enabled"]?.jsonPrimitive?.let { config.sitting.onDoubleSneak.enabled = it.boolean }
            jsonSneak["angle"]?.jsonPrimitive?.let { config.sitting.onDoubleSneak.minPitch = it.double }
            jsonSneak["delay"]?.jsonPrimitive?.let { config.sitting.onDoubleSneak.delay = it.long }
        }
    }
}

internal fun ModConfig.migrate(yamlNode: YamlNode) = FSitConfigMigrator.fromYaml(this, yamlNode)
internal fun ModConfig.migrate(jsonElement: JsonElement) = FSitConfigMigrator.fromJson(this, jsonElement)
