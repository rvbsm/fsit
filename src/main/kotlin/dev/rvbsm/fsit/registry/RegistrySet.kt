package dev.rvbsm.fsit.registry

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.block.Block
import net.minecraft.registry.DefaultedRegistry
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.TagKey

interface RegistryCollection<E> : Collection<RegistryIdentifier> {
    val registry: DefaultedRegistry<E>
    val entries: Map<RegistryIdentifier, E>
    val tags: Map<RegistryIdentifier, TagKey<E>>
}

class RegistrySet<E>(
    override val registry: DefaultedRegistry<E>,
    private val ids: Set<RegistryIdentifier> = emptySet(),
) : RegistryCollection<E>, Set<RegistryIdentifier> by ids {

    override val entries = filterNot { it.isTag }.associateWith { registry[it.value] }
    override val tags = filter { it.isTag }.associateWith { TagKey.of(registry.key, it.value) }

    override fun toString(): String = ids.toString()
    override fun equals(other: Any?): Boolean = ids == other
    override fun hashCode(): Int = ids.hashCode()

    operator fun plus(other: RegistrySet<E>) = RegistrySet(registry, (this as Set<RegistryIdentifier>) + other)

    open class Serializer<E>(private val registry: DefaultedRegistry<E>) : KSerializer<RegistrySet<E>> {
        private val setSerializer = SetSerializer(RegistryIdentifier.serializer())
        override val descriptor get() = setSerializer.descriptor

        override fun serialize(encoder: Encoder, value: RegistrySet<E>) = setSerializer.serialize(encoder, value)
        override fun deserialize(decoder: Decoder) = RegistrySet(registry, setSerializer.deserialize(decoder))
    }
}

fun <E> registrySetOf(registry: DefaultedRegistry<E>, vararg entries: E) =
    RegistrySet(registry, entries.map { RegistryIdentifier(registry.getId(it), isTag = false) }.toSet())

fun <E> registrySetOf(registry: DefaultedRegistry<E>, vararg tags: TagKey<E>) =
    RegistrySet(registry, tags.map { RegistryIdentifier(it.id, isTag = true) }.toSet())

fun registrySetOf(vararg blocks: Block) = registrySetOf(Registries.BLOCK, *blocks)
fun registrySetOf(vararg tags: TagKey<Block>) = registrySetOf(Registries.BLOCK, *tags)

fun <E> Iterable<RegistryIdentifier>.toRegistrySet(registry: DefaultedRegistry<E>) = RegistrySet(registry, toSet())
