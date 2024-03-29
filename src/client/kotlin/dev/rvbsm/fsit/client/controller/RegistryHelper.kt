package dev.rvbsm.fsit.client.controller

import dev.rvbsm.fsit.util.id
import net.minecraft.item.ItemConvertible
import net.minecraft.registry.DefaultedRegistry
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrNull

sealed interface RegistryHelper<T, W> where T : ItemConvertible {
    val registry: DefaultedRegistry<T>

    fun isRegistered(value: String): Boolean

    fun fromStringWrapper(value: String): W

    fun fromString(value: String): T?

    fun fromId(id: Identifier?): T

    fun toString(value: W): String

    fun getIds(): Stream<Identifier>

    fun filterId(value: String): (Identifier) -> Boolean

    fun matchingIdentifiers(value: String): Stream<Identifier> {
        val sep = value.indexOf(Identifier.NAMESPACE_SEPARATOR)

        return getIds().filter(filterId(value)).sorted { id1, id2 ->
            val path = (if (sep == -1) value else value.substring(sep + 1)).lowercase()
            val id1StartsWith = id1.path.lowercase().startsWith(path)
            val id2StartsWith = id2.path.lowercase().startsWith(path)

            when {
                id1StartsWith && id2StartsWith -> id1.compareTo(id2)
                id1StartsWith -> -1
                id2StartsWith -> 1
                else -> id1.compareTo(id2)
            }
        }
    }

    fun validValue(value: String, offset: Long = 0) =
        matchingIdentifiers(value).skip(offset).findFirst().map { "$it" }.getOrNull()

    class Simple<T>(override val registry: DefaultedRegistry<T>) :
        RegistryHelper<T, T> where T : ItemConvertible {
        override fun isRegistered(value: String) = value.id().let { registry.containsId(it) }

        override fun fromStringWrapper(value: String) = fromId(value.id())

        override fun fromString(value: String) = fromStringWrapper(value)

        override fun fromId(id: Identifier?) = registry[id]

        override fun getIds(): Stream<Identifier> = registry.streamEntries().map { it.registryKey().value }

        override fun filterId(value: String): (Identifier) -> Boolean {
            val sep = value.indexOf(Identifier.NAMESPACE_SEPARATOR)
            return if (sep == -1) {
                {
                    (it.path.contains(value) || (registry[it] as ItemConvertible).asItem().name.string.lowercase()
                        .contains(value.lowercase()))
                }
            } else {
                val namespace = value.substring(0, sep)
                val path = value.substring(sep + 1)

                return { it.namespace == namespace && it.path.startsWith(path) }
            }
        }

        override fun toString(value: T) = "${registry.getId(value)}"
    }

    class Tag<T>(override val registry: DefaultedRegistry<T>) :
        RegistryHelper<T, TagKey<T>> where T : ItemConvertible {
        override fun isRegistered(value: String) =
            value.id().let { id -> registry.streamTags().noneMatch { it.id != id } }

        override fun fromStringWrapper(value: String): TagKey<T> =
            registry.streamTags().filter { it.id == value.id() }.findFirst()
                .orElse(TagKey.of(registry.key, value.id()))

        override fun fromString(value: String): T? {
            val tag = fromStringWrapper(value)
            return registry.getEntryList(tag)?.getOrNull()?.firstOrNull()?.value()
        }

        override fun fromId(id: Identifier?): T {
            val tag = TagKey.of(registry.key, id)
            return registry.getEntryList(tag)?.getOrNull()?.firstOrNull()?.value() ?: registry[id]
        }

        override fun getIds(): Stream<Identifier> = registry.streamTags().map { it.id }

        override fun filterId(value: String): (Identifier) -> Boolean {
            val sep = value.indexOf(Identifier.NAMESPACE_SEPARATOR)

            return if (sep == -1) {
                { it.path.contains(value) }
            } else {
                val namespace = value.substring(0, sep)
                val path = value.substring(sep + 1)

                return { it.namespace == namespace && it.path.startsWith(path) }
            }
        }

        override fun toString(value: TagKey<T>) = "${value.id}"
    }
}
