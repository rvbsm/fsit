package dev.rvbsm.fsit.config

import com.charleskorn.kaml.YamlComment
import dev.rvbsm.fsit.registry.RegistrySet
import dev.rvbsm.fsit.registry.registrySetOf
import kotlinx.serialization.Contextual
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.block.Block
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.BlockTags

private const val CURRENT_VERSION = 2

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ModConfig(
    @EncodeDefault internal val version: Int = CURRENT_VERSION,

    // todo: show on the client somehow that server's `use_server` is true
    @YamlComment("Whether to use the server-side configuration.")
    val useServer: Boolean = false,
    val sitting: Sitting = Sitting(),
    val onUse: OnUse = OnUse(),
    val onSneak: OnSneak = OnSneak(),
) {
    companion object {
        val Default = ModConfig()
    }

    class Builder(config: ModConfig = Default) {
        var useServer = config.useServer

        var sittingBehaviour = config.sitting.behaviour
        var sittingShouldCenter = config.sitting.shouldCenter

        var onUseSitting = config.onUse.sitting
        var onUseRiding = config.onUse.riding
        var onUseRange = config.onUse.range
        var onUseCheckSuffocation = config.onUse.checkSuffocation
        var onUseBlocks = config.onUse.blocks

        var onSneakSitting = config.onSneak.sitting
        var onSneakCrawling = config.onSneak.crawling
        var onSneakMinPitch = config.onSneak.minPitch
        var onSneakDelay = config.onSneak.delay

        fun build(): ModConfig {
            onUseRange = onUseRange.coerceIn(1, 4)
            onSneakMinPitch = onSneakMinPitch.coerceIn(-90.0, 90.0)
            onSneakDelay = onSneakDelay.coerceIn(100, 2000)

            return ModConfig(
                useServer = useServer,
                sitting = Sitting(behaviour = sittingBehaviour, shouldCenter = sittingShouldCenter),
                onUse = OnUse(
                    sitting = onUseSitting,
                    riding = onUseRiding,
                    range = onUseRange,
                    checkSuffocation = onUseCheckSuffocation,
                    blocks = onUseBlocks,
                ),
                onSneak = OnSneak(
                    sitting = onSneakSitting,
                    crawling = onSneakCrawling,
                    minPitch = onSneakMinPitch,
                    delay = onSneakDelay,
                ),
            )
        }
    }
}

@Serializable
data class Sitting(
    @YamlComment("Controls sitting behaviour. Possible values: nothing, discard (if no block underneath sitting player), gravity.")
    val behaviour: Behaviour = Behaviour.Gravity,
    @YamlComment("Places seat in the center of the block")
    val shouldCenter: Boolean = false,
) {

    @Serializable
    enum class Behaviour {
        @SerialName("nothing")
        Nothing,

        @SerialName("discard")
        Discard,

        @SerialName("gravity")
        Gravity;

        val shouldMove get() = this == Gravity
        val shouldDiscard get() = this == Discard
    }
}

@Serializable
data class OnUse(
    @YamlComment("Allows to start sitting on specific blocks by interacting with them.")
    val sitting: Boolean = true,
    @YamlComment("Allows to start riding other players by interaction with them.")
    val riding: Boolean = true,

    @YamlComment("The maximum distance to a target to interact.")
    val range: Long = 2,
    @YamlComment("Prevents players from sitting in places where they would suffocate.")
    val checkSuffocation: Boolean = true,
    @Serializable(BlockSetSerializer::class)
    @YamlComment("List of blocks or block types (e.g., \"oak_log\", \"#logs\") that are available to sit on by interacting with them.")
    val blocks: RegistrySet<@Contextual Block> = registrySetOf(BlockTags.SLABS, BlockTags.STAIRS, BlockTags.LOGS)
) {
    object BlockSetSerializer : RegistrySet.Serializer<Block>(Registries.BLOCK)
}

@Serializable
data class OnSneak(
    @YamlComment("Allows to start sitting by double sneaking while looking down.")
    val sitting: Boolean = true,
    @YamlComment("Allows to start crawling by double sneaking near a one-block gap.")
    val crawling: Boolean = true,

    @YamlComment("The minimum angle must be looking down (in degrees) with double sneak.")
    val minPitch: Double = 60.0,
    @YamlComment("The window between sneaks to sit down (in milliseconds).")
    val delay: Long = 600,
)

fun Result<ModConfig>.orDefault() = getOrDefault(ModConfig.Default)
