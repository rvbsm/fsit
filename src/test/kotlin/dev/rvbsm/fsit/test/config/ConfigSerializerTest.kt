package dev.rvbsm.fsit.test.config

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.YamlNamingStrategy
import com.google.common.io.Resources
import dev.rvbsm.fsit.config.ModConfig
import dev.rvbsm.fsit.config.OnSneak
import dev.rvbsm.fsit.config.OnUse
import dev.rvbsm.fsit.config.Sitting
import dev.rvbsm.fsit.config.serialization.ConfigSerializer
import dev.rvbsm.fsit.registry.registrySetOf
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import net.minecraft.Bootstrap
import net.minecraft.SharedConstants
import net.minecraft.block.Blocks
import net.minecraft.registry.tag.BlockTags
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.time.measureTimedValue

@OptIn(ExperimentalSerializationApi::class)
private val jsonSerializer = ConfigSerializer(format = Json {
    ignoreUnknownKeys = true; namingStrategy = JsonNamingStrategy.SnakeCase
})

private val yamlSerializer = ConfigSerializer(
    format = Yaml(
        configuration = YamlConfiguration(
            strictMode = false,
            yamlNamingStrategy = YamlNamingStrategy.SnakeCase,
        )
    )
)

private fun getResource(path: String) = Resources.getResource(path)
private fun readResource(path: String) = getResource(path).readText()

object ConfigSerializerTest {
    private val logger = LoggerFactory.getLogger(ConfigSerializerTest::class.java)

    @JvmStatic
    @BeforeAll
    fun initialize() {
        SharedConstants.createGameVersion()
        Bootstrap.initialize()
    }

    private val expectedConfig by lazy {
        ModConfig(
            useServer = false,
            sitting = Sitting(
                behaviour = Sitting.Behaviour.Nothing,
            ),
            onUse = OnUse(
                sitting = false,
                riding = false,
                range = 1,
                checkSuffocation = true,
                blocks = registrySetOf(Blocks.AMETHYST_BLOCK) + registrySetOf(BlockTags.BEDS, BlockTags.STAIRS),
            ),
            onSneak = OnSneak(
                sitting = false,
                crawling = true,
                minPitch = 42.0,
                delay = 999,
            ),
        )
    }

    private val expectedAncientConfigs by lazy {
        mapOf(
            "v1.3.0" to ModConfig(
                onSneak = OnSneak(
                    sitting = false,
                    minPitch = 42.0,
                    delay = 999,
                ),
                onUse = OnUse(
                    sitting = false,
                    blocks = registrySetOf(Blocks.AMETHYST_BLOCK) + registrySetOf(BlockTags.BEDS, BlockTags.STAIRS),
                    riding = false,
                ),
            ),

            "v1.3.1" to ModConfig(
                onSneak = OnSneak(
                    sitting = false,
                    minPitch = 42.0,
                    delay = 999,
                ),
                onUse = OnUse(
                    sitting = false,
                    range = 1,
                    blocks = registrySetOf(Blocks.AMETHYST_BLOCK) + registrySetOf(BlockTags.BEDS, BlockTags.STAIRS),
                    riding = false,
                ),
            ),

            "v1.4.0" to ModConfig(
                onSneak = OnSneak(
                    sitting = false,
                    minPitch = 42.0,
                    delay = 999,
                ),
                onUse = OnUse(
                    sitting = false,
                    range = 1,
                    blocks = registrySetOf(Blocks.AMETHYST_BLOCK) + registrySetOf(BlockTags.BEDS, BlockTags.STAIRS),
                    riding = false,
                ),
            ),
        )
    }

    private val expectedLegacyConfigs by lazy {
        mapOf(
            "v2.0.0" to ModConfig(
                useServer = true,
                onUse = OnUse(
                    sitting = false,
                    range = 1,
                    blocks = registrySetOf(Blocks.AMETHYST_BLOCK) + registrySetOf(BlockTags.BEDS, BlockTags.STAIRS),
                    riding = false,
                ),
            ),

            "v2.1.0" to ModConfig(
                useServer = true,
                sitting = Sitting(
//                    applyGravity = false,
                ),
                onUse = OnUse(
                    sitting = false,
                    range = 1,
                    blocks = registrySetOf(Blocks.AMETHYST_BLOCK) + registrySetOf(BlockTags.BEDS, BlockTags.STAIRS),
                    riding = false,
                ),
                onSneak = OnSneak(
                    sitting = false,
                    minPitch = 42.0,
                    delay = 999,
                ),
            ),

            "v2.2.0" to ModConfig(
                useServer = true,
                sitting = Sitting(
//                    applyGravity = false,
//                    allowInAir = true,
                ),
                onUse = OnUse(
                    sitting = false,
                    riding = false,
                    range = 1,
                    checkSuffocation = false,
                    blocks = registrySetOf(Blocks.AMETHYST_BLOCK) + registrySetOf(BlockTags.BEDS, BlockTags.STAIRS),
                ),
                onSneak = OnSneak(
                    sitting = false,
                    crawling = false,
                    minPitch = 42.0,
                    delay = 999,
                ),
            ),

            "v2.5.0" to ModConfig(
                useServer = true,
                sitting = Sitting(
//                    applyGravity = false,
//                    allowInAir = true,
                ),
                onUse = OnUse(
                    sitting = false,
                    riding = false,
                    range = 1,
                    checkSuffocation = false,
                    blocks = registrySetOf(Blocks.AMETHYST_BLOCK) + registrySetOf(BlockTags.BEDS, BlockTags.STAIRS),
                ),
                onSneak = OnSneak(
                    sitting = false,
                    crawling = false,
                    minPitch = 42.0,
                    delay = 999,
                ),
            ),
        )
    }

    @Test
    fun `test JSON`() {
        val actualConfig = measureTimedValue { jsonSerializer.decode(jsonSerializer.encode(expectedConfig)) }

        logger.info("(JSON) deserialization time: ${actualConfig.duration}")
        assertEquals(expectedConfig, actualConfig.value)
    }

    @Test
    fun `test YAML`() {
        val actualConfig = measureTimedValue { yamlSerializer.decode(yamlSerializer.encode(expectedConfig)) }

        logger.info("(YAML) deserialization time: ${actualConfig.duration}")
        assertEquals(expectedConfig, actualConfig.value)
    }

    @Test
    fun `test JSON ancient (mod v1)`() {
        expectedAncientConfigs.forEach { (version, expectedConfig) ->
            val jsonConfig = readResource("configs/$version.json")
            val actualConfig = measureTimedValue { jsonSerializer.decode(jsonConfig) }

            logger.info("(JSON legacy $version) deserialization time: ${actualConfig.duration}")
            assertEquals(expectedConfig, actualConfig.value, "$version (you have no legacy bozo)")
        }
    }

    @Test
    fun `test JSON legacy (mod v2)`() {
        expectedLegacyConfigs.forEach { (version, expectedConfig) ->
            val jsonConfig = readResource("configs/$version.json")
            val actualConfig = measureTimedValue { jsonSerializer.decode(jsonConfig) }

            logger.info("(JSON $version) deserialization time: ${actualConfig.duration}")
            assertEquals(expectedConfig, actualConfig.value, version)
        }
    }

    @Test
    fun `test YAML legacy (mod v2)`() {
        expectedLegacyConfigs.forEach { (version, expectedConfig) ->
            val yamlConfig = readResource("configs/$version.yml")
            val actualConfig = measureTimedValue { yamlSerializer.decode(yamlConfig) }

            logger.info("(YAML $version) deserialization time: ${actualConfig.duration}")
            assertEquals(expectedConfig, actualConfig.value, version)
        }
    }

    @Test
    fun `test JSON ancient (mod v1) + legacy (mod v2)`() {
        val jsonConfig = readResource("configs/v1.4.0+v2.1.0.json")
        val expectedConfig = ModConfig(
            onSneak = OnSneak(
                sitting = true,
                minPitch = 69.0,
                delay = 420,
            ),
        )
        val actualConfig = jsonSerializer.decode(jsonConfig)

        assertEquals(expectedConfig, actualConfig)
    }
}
