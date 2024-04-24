package dev.rvbsm.fsit.config

import com.charleskorn.kaml.*
import dev.rvbsm.fsit.config.container.BlockContainer
import dev.rvbsm.fsit.config.migration.BooleanProperty
import dev.rvbsm.fsit.config.migration.ContainerProperty
import dev.rvbsm.fsit.config.migration.DoubleProperty
import dev.rvbsm.fsit.config.migration.LongProperty
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import net.minecraft.block.Block
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

private val containerMigrations = setOf(
    ContainerProperty<Block, BlockContainer>("sittable.blocks", ContainerProperty.Type.ENTRIES),
    ContainerProperty<Block, BlockContainer>("sittable.tags", ContainerProperty.Type.TAGS),
    ContainerProperty<Block, BlockContainer>("sittable.materials", ContainerProperty.Type.CONTAINER),
    ContainerProperty<Block, BlockContainer>("sitting.blocks", ContainerProperty.Type.CONTAINER),
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

        containerMigrations.forEach {
            it.container = config.onUse.blocks

            if (it.migrate(yamlConfig)) {
                logger.info("Migrated '$it'")
            }
        }
    }

    internal fun migrateJson(config: ModConfig, jsonElement: JsonElement) {
        val jsonConfig = jsonElement.jsonObject

        migrations.forEach {
            it.config = config

            it.migrate(jsonConfig)
        }

        containerMigrations.forEach {
            it.container = config.onUse.blocks

            it.migrate(jsonConfig)
        }
    }
}

internal fun ModConfig.migrate(yamlNode: YamlNode) = FSitConfigMigrator.migrateYaml(this, yamlNode)
internal fun ModConfig.migrate(jsonElement: JsonElement) = FSitConfigMigrator.migrateJson(this, jsonElement)
