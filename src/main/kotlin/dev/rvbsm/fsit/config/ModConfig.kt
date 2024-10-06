package dev.rvbsm.fsit.config

import com.charleskorn.kaml.YamlComment
import dev.rvbsm.fsit.registry.RegistrySet
import dev.rvbsm.fsit.registry.registrySetOf
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient
import net.minecraft.block.Block
import net.minecraft.registry.tag.BlockTags
import java.nio.file.Path

internal const val CURRENT_VERSION = 1

@Serializable
data class ModConfig(
    @Transient internal val path: Path? = null,
    internal val version: Int = CURRENT_VERSION,

    // todo: show on the client somehow that server's `use_server` is true
    @YamlComment("Whether to use the server-side configuration.")
    var useServer: Boolean = false,
    val sitting: Sitting = Sitting(),
    val onUse: OnUse = OnUse(),
    val onDoubleSneak: OnDoubleSneak = OnDoubleSneak(),
) {
    init {
        onUse.range = onUse.range.coerceIn(1L..4)
        onDoubleSneak.minPitch = onDoubleSneak.minPitch.coerceIn(-90.0..90.0)
        onDoubleSneak.delay = onDoubleSneak.delay.coerceIn(100L..2000)
    }
}

@Serializable
data class Sitting(
    @YamlComment("Controls sitting behaviour. Possible values: nothing, discard (if no block underneath sitting player), gravity.")
    var behaviour: Behaviour = Behaviour.Gravity,
) {

    @Serializable
    enum class Behaviour {
        @SerialName("nothing") Nothing,
        @SerialName("discard") Discard,
        @SerialName("gravity") Gravity;

        val shouldMove get() = this == Gravity
        val shouldDiscard get() = this == Discard
    }
}

@Serializable
data class OnUse(
    @YamlComment("Allows to start sitting on specific blocks by interacting with them.")
    var sitting: Boolean = true,
    @YamlComment("Allows to start riding other players by interaction with them.")
    var riding: Boolean = true,

    @YamlComment("The maximum distance to a target to interact.")
    var range: Long = 2,
    @YamlComment("Prevents players from sitting in places where they would suffocate.")
    var checkSuffocation: Boolean = true,
    @Serializable(RegistrySet.BlockSerializer::class)
    @YamlComment("List of blocks or block types (e.g., \"oak_log\", \"#logs\") that are available to sit on by interacting with them.")
    var blocks: RegistrySet<@Contextual Block> = registrySetOf(BlockTags.SLABS, BlockTags.STAIRS, BlockTags.LOGS)
)

@Serializable
data class OnDoubleSneak(
    @YamlComment("Allows to start sitting by double sneaking while looking down.")
    var sitting: Boolean = true,
    @YamlComment("Allows to start crawling by double sneaking near a one-block gap.")
    var crawling: Boolean = true,

    @YamlComment("The minimum angle must be looking down (in degrees) with double sneak.")
    var minPitch: Double = 66.6,
    @YamlComment("The window between sneaks to sit down (in milliseconds).")
    var delay: Long = 600,
)
