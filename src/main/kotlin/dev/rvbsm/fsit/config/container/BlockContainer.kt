package dev.rvbsm.fsit.config.container

import kotlinx.serialization.Serializable
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.TagKey

@Serializable(BlockContainer.Serializer::class)
class BlockContainer(
    blocks: MutableSet<Block> = mutableSetOf(),
    tags: MutableSet<TagKey<Block>> = mutableSetOf(),
) : Container<Block, BlockState>(Registries.BLOCK, blocks, tags) {
    override fun test(state: BlockState) = entries.any { state.isOf(it) } || tags.any { state.isIn(it) }

    class Serializer : Container.Serializer<Block, BlockContainer>(::BlockContainer, Registries.BLOCK)
}
