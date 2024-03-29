package dev.rvbsm.fsit.config

import dev.rvbsm.fsit.util.id
import dev.rvbsm.fsit.util.literal
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text

sealed interface Material {
    override fun toString(): String
    fun asText(): Text
    fun corresponds(blockState: BlockState): Boolean

    companion object {
        fun fromString(value: String) = if (value.startsWith('#')) {
            TagEntry(TagKey.of(Registries.BLOCK.key, value.drop(1).id()))
        } else {
            BlockEntry(Registries.BLOCK.get(value.id()))
        }
    }

    class BlockEntry(val block: Block) : Material {
        override fun toString() = "${Registries.BLOCK.getId(block)}"
        override fun asText(): Text = block.asItem().name
        override fun corresponds(blockState: BlockState) = blockState.isOf(block)
    }

    class TagEntry(val tag: TagKey<Block>) : Material {
        override fun toString() = "#${tag.id}"
        override fun asText() = "${tag.id}".literal()
        override fun corresponds(blockState: BlockState) = blockState.isIn(tag)
    }

    object Serializer : KSerializer<Material> {
        override val descriptor = PrimitiveSerialDescriptor("Material", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder) = fromString(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: Material) = encoder.encodeString("$value")
    }
}

fun Block.asMaterial() = Material.BlockEntry(this)
fun TagKey<Block>.asMaterial() = Material.TagEntry(this)

fun Iterable<Block>.asBlockEntries() = map { it.asMaterial() }
fun Iterable<TagKey<Block>>.asTagEntries() = map { it.asMaterial() }

fun Iterable<Material>.getBlocks() = filterIsInstance<Material.BlockEntry>().map { it.block }
fun Iterable<Material>.getTags() = filterIsInstance<Material.TagEntry>().map { it.tag }

inline fun <reified M> MutableSet<Material>.updateWith(materials: Iterable<M>) where M : Material {
    removeAll { it is M }
    addAll(materials.toSet())
}
