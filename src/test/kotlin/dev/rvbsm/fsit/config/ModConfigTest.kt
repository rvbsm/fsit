package dev.rvbsm.fsit.config

import com.charleskorn.kaml.YamlNamingStrategy
import com.google.common.io.Resources
import dev.rvbsm.fsit.registry.registrySetOf
import dev.rvbsm.fsit.serialization.StringDataSerializer
import dev.rvbsm.fsit.serialization.Yaml
import dev.rvbsm.fsit.serialization.asSerializer
import dev.rvbsm.fsit.serialization.decode
import dev.rvbsm.fsit.serialization.encode
import dev.rvbsm.fsit.serialization.migration.version
import dev.rvbsm.fsit.serialization.migration.withMigration
import dev.rvbsm.fsit.serialization.withDefault
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import net.minecraft.Bootstrap
import net.minecraft.SharedConstants
import net.minecraft.block.Blocks
import net.minecraft.registry.tag.BlockTags
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.time.measureTimedValue

@PublishedApi
internal val logger = LoggerFactory.getLogger(ModConfigTest::class.java)

@OptIn(ExperimentalSerializationApi::class)
@Suppress("json_format_redundant")
private val jsonSerializer = Json {
    ignoreUnknownKeys = true
    namingStrategy = JsonNamingStrategy.SnakeCase

    withMigration<ModConfig>(configSchemas) { if ("config_version" in this) -1 else version }
    withDefault(::ModConfig)
}.asSerializer()

private val yamlSerializer = Yaml {
    strictMode = false
    yamlNamingStrategy = YamlNamingStrategy.SnakeCase

    withMigration<ModConfig>(schemas = configSchemas)
    withDefault(::ModConfig)
}.asSerializer()

object ModConfigTest {

    @JvmStatic
    @BeforeAll
    fun initialize() {
        SharedConstants.createGameVersion()
        Bootstrap.initialize()

        configs
    }

    @Test
    fun testSerialization() = runBlocking {
        val expected = ModConfig(
            useServer = true,
            sitting = Sitting(behaviour = Sitting.Behaviour.Nothing, shouldCenter = true),
            onUse = OnUse(
                sitting = false,
                riding = false,
                range = 1,
                checkSuffocation = false,
                blocks = registrySetOf(Blocks.AMETHYST_BLOCK) + registrySetOf(BlockTags.BEDS, BlockTags.STAIRS)
            ),
            onSneak = OnSneak(sitting = false, crawling = false, minPitch = 42.0, delay = 999)
        )

        val jsonEncoded = jsonSerializer.encode(expected).getOrThrow()
        val jsonActual = jsonSerializer.decode<ModConfig>(jsonEncoded).getOrThrow()
        assertEquals(expected, jsonActual)

        val yamlEncoded = yamlSerializer.encode(expected).getOrThrow()
        val yamlActual = yamlSerializer.decode<ModConfig>(yamlEncoded).getOrThrow()
        assertEquals(expected, yamlActual)
    }

    @ParameterizedTest
    @MethodSource("getJsonConfigs", "getYamlConfigs")
    fun StringDataSerializer<*>.testMigrations(data: String, expected: ModConfig, where: String) = runBlocking {
        val actual = measureTimedValue { decode<ModConfig>(data) }

        logger.info("[{}] Deserialized in {}", where, actual.duration)
        assertEquals(expected, actual.value.getOrThrow())
    }

    private fun StringDataSerializer<*>.argument(type: String, version: String, config: ModConfig) =
        Arguments.of(this, Resources.getResource("config/$version.$type").readText(), config, "$type $version")

    @JvmStatic
    fun getJsonConfigs() = configs.map { (version, config) ->
        jsonSerializer.argument("json", version, config)
    }

    @JvmStatic
    fun getYamlConfigs() = configs.filterKeys { !it.startsWith("v1") }.map { (version, config) ->
        yamlSerializer.argument("yml", version, config)
    }

    private val configs by lazy {
        mapOf(
            "v1.3.0" to ModConfig(
                onUse = OnUse(
                    sitting = false,
                    blocks = registrySetOf(Blocks.AMETHYST_BLOCK) + registrySetOf(BlockTags.BEDS, BlockTags.STAIRS),
                    riding = false
                ), onSneak = OnSneak(sitting = false, minPitch = 42.0, delay = 999)
            ),
            "v1.3.1" to ModConfig(
                onUse = OnUse(
                    sitting = false,
                    range = 1,
                    blocks = registrySetOf(Blocks.AMETHYST_BLOCK) + registrySetOf(BlockTags.BEDS, BlockTags.STAIRS),
                    riding = false
                ), onSneak = OnSneak(sitting = false, minPitch = 42.0, delay = 999)
            ),
            "v1.4.0" to ModConfig(
                onUse = OnUse(
                    sitting = false,
                    range = 1,
                    blocks = registrySetOf(Blocks.AMETHYST_BLOCK) + registrySetOf(BlockTags.BEDS, BlockTags.STAIRS),
                    riding = false
                ), onSneak = OnSneak(sitting = false, minPitch = 42.0, delay = 999)
            ),

            "v2.0.0" to ModConfig(
                useServer = true, onUse = OnUse(
                    sitting = false,
                    range = 1,
                    blocks = registrySetOf(Blocks.AMETHYST_BLOCK) + registrySetOf(BlockTags.BEDS, BlockTags.STAIRS),
                    riding = false
                )
            ),
            "v2.1.0" to ModConfig(
                useServer = true, sitting = Sitting(behaviour = Sitting.Behaviour.Discard), onUse = OnUse(
                    sitting = false,
                    range = 1,
                    blocks = registrySetOf(Blocks.AMETHYST_BLOCK) + registrySetOf(BlockTags.BEDS, BlockTags.STAIRS),
                    riding = false
                ), onSneak = OnSneak(sitting = false, minPitch = 42.0, delay = 999)
            ),
            "v2.2.0" to ModConfig(
                useServer = true, sitting = Sitting(behaviour = Sitting.Behaviour.Nothing), onUse = OnUse(
                    sitting = false,
                    riding = false,
                    range = 1,
                    checkSuffocation = false,
                    blocks = registrySetOf(Blocks.AMETHYST_BLOCK) + registrySetOf(BlockTags.BEDS, BlockTags.STAIRS)
                ), onSneak = OnSneak(sitting = false, crawling = false, minPitch = 42.0, delay = 999)
            ),
            "v2.5.0" to ModConfig(
                useServer = true, sitting = Sitting(behaviour = Sitting.Behaviour.Nothing), onUse = OnUse(
                    sitting = false,
                    riding = false,
                    range = 1,
                    checkSuffocation = false,
                    blocks = registrySetOf(Blocks.AMETHYST_BLOCK) + registrySetOf(BlockTags.BEDS, BlockTags.STAIRS)
                ), onSneak = OnSneak(sitting = false, crawling = false, minPitch = 42.0, delay = 999)
            ),
            "v2.6.0" to ModConfig(
                useServer = true,
                sitting = Sitting(behaviour = Sitting.Behaviour.Nothing, shouldCenter = true),
                onUse = OnUse(
                    sitting = false,
                    riding = false,
                    range = 1,
                    checkSuffocation = false,
                    blocks = registrySetOf(Blocks.AMETHYST_BLOCK) + registrySetOf(BlockTags.BEDS, BlockTags.STAIRS)
                ),
                onSneak = OnSneak(sitting = false, crawling = false, minPitch = 42.0, delay = 999)
            ),
        )
    }
}
