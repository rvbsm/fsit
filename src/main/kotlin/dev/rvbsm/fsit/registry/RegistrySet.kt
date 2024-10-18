package dev.rvbsm.fsit.registry

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.block.Block
import net.minecraft.registry.DefaultedRegistry
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.TagKey

interface RegistrySet<E> : Set<RegistryIdentifier> {
    val registry: DefaultedRegistry<E>
    val entries: Collection<E>
    val tags: Collection<TagKey<E>>

    sealed class Serializer<E>(private val registry: DefaultedRegistry<E>) : KSerializer<RegistrySet<E>> {
        private val setSerializer = SetSerializer(RegistryIdentifier.serializer())
        override val descriptor = setSerializer.descriptor

        override fun deserialize(decoder: Decoder) =
            RegistryLinkedHashSet(registry, decoder.decodeSerializableValue(setSerializer))

        override fun serialize(encoder: Encoder, value: RegistrySet<E>) {
            encoder.encodeSerializableValue(setSerializer, value)
        }
    }

    object BlockSerializer : Serializer<Block>(Registries.BLOCK)
}

@Serializable
class RegistryLinkedHashSet<E>(
    override val registry: DefaultedRegistry<E>,
) : RegistrySet<E>, LinkedHashSet<RegistryIdentifier>() {

    constructor(registry: DefaultedRegistry<E>, ids: Collection<RegistryIdentifier>) : this(registry) {
        addAll(ids.filterNotDefault())
    }

    override val entries: List<E> get() = filterNot { it.isTag }.map { registry[it.id] }
    override val tags: List<TagKey<E>> get() = filter { it.isTag }.map { TagKey.of(registry.key, it.id) }

    operator fun plus(other: RegistrySet<E>) =
        RegistryLinkedHashSet(registry, (this as Set<RegistryIdentifier>) + other)
}

fun <E> registrySetOf(registry: DefaultedRegistry<E>, vararg entries: E) =
    RegistryLinkedHashSet(registry).apply {
        entries.forEach { this += RegistryIdentifier(registry.getId(it), isTag = false) }
    }

fun <E> registrySetOf(registry: DefaultedRegistry<E>, vararg tags: TagKey<E>) =
    RegistryLinkedHashSet(registry).apply {
        tags.forEach { this += RegistryIdentifier(it.id, isTag = true) }
    }

fun registrySetOf(vararg blocks: Block) = registrySetOf(Registries.BLOCK, *blocks)
fun registrySetOf(vararg tags: TagKey<Block>) = registrySetOf(Registries.BLOCK, *tags)

fun <E> Iterable<RegistryIdentifier>.toRegistrySet(registry: DefaultedRegistry<E>) =
    toCollection(RegistryLinkedHashSet(registry))
