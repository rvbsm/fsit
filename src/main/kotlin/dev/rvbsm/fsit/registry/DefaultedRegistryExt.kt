package dev.rvbsm.fsit.registry

import net.minecraft.item.ItemConvertible
import net.minecraft.registry.DefaultedRegistry
import kotlin.streams.asSequence

//? if >=1.21.2-alpha.0
/*private val net.minecraft.registry.entry.RegistryEntryList.Named<*>.id get() = tag.id*/

fun DefaultedRegistry<*>.matchingIdentifiers(value: String): Sequence<RegistryIdentifier> {
    val registryId = RegistryIdentifier.of(value)
    val input = if (registryId.isTag) value.drop(1) else value

    val ids = streamTags().map {
        RegistryIdentifier(it.id, true)
    }.asSequence() + if (!registryId.isTag) streamEntries().map {
        RegistryIdentifier(it.registryKey().value, false)
    }.asSequence() else emptySequence()

    return ids.filter {
        it == registryId
            || it.id.path.contains(registryId.id.path)
            || (this[it.id] as? ItemConvertible)?.asItem()?.name?.string?.contains(input, ignoreCase = true) ?: false
    }.sortedWith { id1, id2 ->
        val id1StartsWith = id1.id.path.startsWith(registryId.id.path)
        val id2StartsWith = id2.id.path.startsWith(registryId.id.path)

        when {
            id1StartsWith && id2StartsWith -> id1.id.compareTo(id2.id)
            id1StartsWith -> -1
            id2StartsWith -> 1
            else -> id1.id.compareTo(id2.id)
        }
    }
}

operator fun DefaultedRegistry<*>.contains(id: RegistryIdentifier) = if (id.isTag) {
    val tagsIterator = streamTags().iterator()
    !tagsIterator.hasNext() || tagsIterator.asSequence().any { it.id == id.id }
} else containsId(id.id)

fun <T> DefaultedRegistry<T>.find(string: String) = find(RegistryIdentifier.of(string))

fun <T> DefaultedRegistry<T>.find(id: RegistryIdentifier): T {
    return if (id.isTag) {
        val tag = streamTags().asSequence().find { it.id == id.id } ?: return this[defaultId]

        //? if <=1.21.1
        getEntryList(tag).orElse(null)?.firstOrNull()?.value() ?: this[defaultId]
        //? if >=1.21.2-alpha.0
        /*tag.firstOrNull()?.value() ?: this[defaultId]*/
    } else this[id.id]
}
