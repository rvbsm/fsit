package dev.rvbsm.fsit.util

import net.minecraft.block.Block
import net.minecraft.registry.DefaultedRegistry
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.TagKey

interface RegistrySet<E> : Set<String> {
    val registry: DefaultedRegistry<E>
    val entries: Set<E>
    val tags: Set<TagKey<E>>
}

class RegistryLinkedHashSet<E>(
    override val registry: DefaultedRegistry<E>,
    vararg ids: String,
) : RegistrySet<E>, LinkedHashSet<String>(ids.toSet()) {
    override val entries: Set<E> = registry.parseEntries(ids.asIterable()).toSet()
    override val tags: Set<TagKey<E>> = registry.parseTags(ids.asIterable()).toSet()
}

private fun <E> Array<E>.asIds(registry: DefaultedRegistry<in E>) = map { registry.getId(it).asString() }.toTypedArray()
private fun <E> Array<out TagKey<E>>.asIds() = map { '#' + it.id.asString() }.toTypedArray()

internal fun <E> registrySetOf(registry: DefaultedRegistry<E>, vararg ids: String) =
    RegistryLinkedHashSet(registry, *ids)

internal fun <E> registrySetOf(registry: DefaultedRegistry<E>, vararg entries: E) =
    registrySetOf(registry, *entries.asIds(registry))

internal fun <E> registrySetOf(registry: DefaultedRegistry<E>, vararg tags: TagKey<E>) =
    registrySetOf(registry, *tags.asIds())

internal fun registrySetOf(vararg blocks: Block) = registrySetOf(Registries.BLOCK, *blocks)
internal fun registrySetOf(vararg tags: TagKey<Block>) = registrySetOf(Registries.BLOCK, *tags)

fun <E> Iterable<String>.toRegistrySet(registry: DefaultedRegistry<E>): RegistrySet<E> {
    return if (this is Collection) {
        registrySetOf(registry, *toTypedArray())
    } else toCollection(RegistryLinkedHashSet(registry))
}
