package dev.rvbsm.fsit.config.container

import dev.rvbsm.fsit.util.id
import dev.rvbsm.fsit.util.literal
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text

sealed interface BlockContainer : Container {
    fun test(state: BlockState): Boolean

    class BlockEntry(val block: Block) : BlockContainer {
        override fun toString() = "${Registries.BLOCK.getId(block)}"
        override fun asText(): Text = block.asItem().name
        override fun test(state: BlockState) = state.isOf(block)
    }

    class TagEntry(val tag: TagKey<Block>) : BlockContainer {
        override fun toString() = "#${tag.id}"
        override fun asText() = "${tag.id}".literal()
        override fun test(state: BlockState) = state.isIn(tag)
    }

    object Serializer : Container.Serializer<BlockContainer> {
        override val descriptor = PrimitiveSerialDescriptor("BlockContainer", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder) = decoder.decodeString().let {
            if (it.startsWith('#')) {
                TagEntry(TagKey.of(Registries.BLOCK.key, it.drop(1).id()))
            } else {
                BlockEntry(Registries.BLOCK.get(it.id()))
            }
        }
    }
}

fun Block.asContainer() = BlockContainer.BlockEntry(this)
fun TagKey<Block>.asContainer() = BlockContainer.TagEntry(this)

fun Iterable<Block>.asEntries() = map { it.asContainer() }
fun Iterable<TagKey<Block>>.asTags() = map { it.asContainer() }

fun Iterable<BlockContainer>.getEntries() = filterIsInstance<BlockContainer.BlockEntry>().map { it.block }
fun Iterable<BlockContainer>.getTags() = filterIsInstance<BlockContainer.TagEntry>().map { it.tag }
