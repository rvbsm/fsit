package dev.rvbsm.fsit.config.container

import dev.rvbsm.fsit.util.asString
import dev.rvbsm.fsit.util.id
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.item.ItemConvertible
import net.minecraft.registry.DefaultedRegistry
import net.minecraft.registry.tag.TagKey
import java.util.function.Predicate

sealed class Container<E : ItemConvertible, S>(
    val registry: DefaultedRegistry<E>,
    val entries: MutableSet<E> = mutableSetOf(),
    val tags: MutableSet<TagKey<E>> = mutableSetOf(),
) : Predicate<S> {
    abstract override fun test(state: S): Boolean

    override fun toString(): String {
        return (entries + tags).toString()
    }

    fun updateEntries(newEntries: Iterable<E>) {
        entries.removeAll { it !in newEntries }
        entries.addAll(newEntries)
    }

    fun updateEntries(ids: Iterable<String>) {
        updateEntries(ids.parseEntries(registry))
    }

    fun updateTags(newTags: Iterable<TagKey<E>>) {
        tags.removeAll { it !in newTags }
        tags.addAll(newTags)
    }

    fun updateTags(ids: Iterable<String>) {
        updateTags(ids.parseTags(registry))
    }

    sealed class Serializer<E : ItemConvertible, C : Container<E, *>>(
        val constructor: (MutableSet<E>, MutableSet<TagKey<E>>) -> C, private val registry: DefaultedRegistry<E>
    ) : KSerializer<C> {
        private val setSerializer = SetSerializer(String.serializer())
        override val descriptor = setSerializer.descriptor

        override fun deserialize(decoder: Decoder): C {
            val ids = decoder.decodeSerializableValue(setSerializer)

            val entries = ids.parseEntries(registry)
            val tags = ids.parseTags(registry)

            return constructor(entries.toMutableSet(), tags.toMutableSet())
        }

        override fun serialize(encoder: Encoder, value: C) {
            val entries = value.entries.map { registry.getId(it).asString() }
            val tags = value.tags.map { '#' + it.id.asString() }

            encoder.encodeSerializableValue(setSerializer, (tags + entries).toSet())
        }
    }

    companion object {
        fun <E> Iterable<String>.parseEntries(registry: DefaultedRegistry<E>) =
            filter { !it.startsWith('#') }.map { registry[it.id()] }

        fun <E> Iterable<String>.parseTags(registry: DefaultedRegistry<E>) =
            filter { it.startsWith('#') }.map { TagKey.of(registry.key, it.drop(1).id()) }
    }
}
