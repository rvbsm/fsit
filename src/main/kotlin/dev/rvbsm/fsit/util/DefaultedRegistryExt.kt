package dev.rvbsm.fsit.util

import net.minecraft.item.ItemConvertible
import net.minecraft.registry.DefaultedRegistry
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import kotlin.jvm.optionals.getOrNull
import kotlin.streams.asSequence

fun <T> DefaultedRegistry<T>.parseEntries(ids: Iterable<String>) =
    ids.filter { !it.startsWith('#') }.map { this[it.id()] }

fun <T> DefaultedRegistry<T>.parseTags(ids: Iterable<String>) =
    ids.filter { it.startsWith('#') }.map { TagKey.of(key, it.drop(1).id()) }

fun DefaultedRegistry<*>.matchingIdentifiers(value: String): Sequence<String> {
    val isTag = value.startsWith('#')
    val input = (if (isTag) value.drop(1) else value).lowercase()
    val sep = input.indexOf(Identifier.NAMESPACE_SEPARATOR)

    val ids = streamTags().map { it.id }.map { id -> id to "#$id" }.asSequence() + if (!isTag) {
        streamEntries().map { it.registryKey().value }.map { id -> id to "$id" }.asSequence()
    } else emptySequence()

    return ids.filter {
        if (sep == -1) {
            it.first.path.contains(input) || (getOrEmpty(it.first).getOrNull() as? ItemConvertible
                ?: return@filter false).asItem().name.string.lowercase().contains(input)
        } else {
            val namespace = input.substring(0, sep)
            val path = input.substring(sep + 1)

            it.first.namespace == namespace && it.first.path.startsWith(path)
        }
    }.sortedWith { id1, id2 ->
        val path = (if (sep == -1) input else input.substring(sep + 1)).lowercase()
        val id1StartsWith = id1.first.path.lowercase().startsWith(path)
        val id2StartsWith = id2.first.path.lowercase().startsWith(path)

        when {
            id1StartsWith && id2StartsWith -> id1.first.compareTo(id2.first)
            id1StartsWith -> -1
            id2StartsWith -> 1
            else -> id1.first.compareTo(id2.first)
        }
    }.map { it.second }
}

fun <T> DefaultedRegistry<T>.find(string: String): T? {
    if (string.startsWith('#')) {
        val id = string.drop(1).id() ?: return null
        return find(id)
    }

    val id = string.id() ?: return null
    return get(id)
}

fun <T> DefaultedRegistry<T>.find(id: Identifier): T? {
    val tag = streamTags().filter { it.id == id }.findFirst().orElse(TagKey.of(key, id))

    return getEntryList(tag).getOrNull()?.firstOrNull()?.value()
}
