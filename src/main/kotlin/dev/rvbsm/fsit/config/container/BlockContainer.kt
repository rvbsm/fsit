package dev.rvbsm.fsit.config.container

import dev.rvbsm.fsit.util.asString
import dev.rvbsm.fsit.util.id
import dev.rvbsm.fsit.util.literal
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text

@Serializable(BlockContainer.Serializer::class)
sealed interface BlockContainer : Container {
    fun test(state: BlockState): Boolean

    class BlockEntry(val block: Block) : BlockContainer {
        override fun toString() = Registries.BLOCK.getId(block).asString()
        override fun asText(): Text = block.asItem().name
        override fun test(state: BlockState) = state.isOf(block)

        companion object {
            fun fromString(string: String) = BlockEntry(Registries.BLOCK[string.id()])
        }
    }

    class TagEntry(val tag: TagKey<Block>) : BlockContainer {
        override fun toString() = "#${tag.id.asString()}"
        override fun asText() = "#${tag.id}".literal()
        override fun test(state: BlockState) = state.isIn(tag)

        companion object {
            fun fromString(string: String) = TagEntry(TagKey.of(Registries.BLOCK.key, string.drop(1).id()))
        }
    }

    object Serializer : Container.Serializer<BlockContainer> {
        override val descriptor = PrimitiveSerialDescriptor("BlockContainer", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder) = fromString(decoder.decodeString())
    }

    companion object {
        fun fromString(string: String) = if (string.startsWith('#')) {
            TagEntry.fromString(string)
        } else {
            BlockEntry.fromString(string)
        }
    }
}

fun Block.asContainer() = BlockContainer.BlockEntry(this)
fun TagKey<Block>.asContainer() = BlockContainer.TagEntry(this)

fun Iterable<Block>.asEntries() = map { it.asContainer() }
fun Iterable<TagKey<Block>>.asTags() = map { it.asContainer() }

fun Iterable<BlockContainer>.getEntries() = filterIsInstance<BlockContainer.BlockEntry>().map { it.block }
fun Iterable<BlockContainer>.getTags() = filterIsInstance<BlockContainer.TagEntry>().map { it.tag }
