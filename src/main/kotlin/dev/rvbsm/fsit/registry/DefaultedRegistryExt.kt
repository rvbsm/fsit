/**
 * Yanked, generalized, and Kotlin-ified version of [dev.isxander.yacl3.gui.utils.ItemRegistryHelper]
 */

package dev.rvbsm.fsit.registry

import dev.isxander.yacl3.gui.utils.MiscUtil
import net.minecraft.item.ItemConvertible
import net.minecraft.registry.DefaultedRegistry
import net.minecraft.util.Identifier
import kotlin.streams.asSequence

//? if >=1.21.2
/*private val net.minecraft.registry.entry.RegistryEntryList.Named<*>.id get() = tag.id*/

/**
 * @see dev.isxander.yacl3.gui.utils.ItemRegistryHelper.isRegisteredItem
 */
fun <T : ItemConvertible> DefaultedRegistry<T>.isRegistered(identifier: String): Boolean {
    val registryId = RegistryIdentifier.of(identifier.lowercase())

    return if (registryId.isTag) {
        val tagsIterator = streamTags().iterator()
        !tagsIterator.hasNext() || tagsIterator.asSequence().any { it.id == registryId.value }
    } else {
        containsId(registryId.value)
    }
}

/**
 * @see dev.isxander.yacl3.gui.utils.ItemRegistryHelper.getMatchingItemIdentifiers
 */
fun <T : ItemConvertible> DefaultedRegistry<T>.getMatchingIdentifiers(value: String): Sequence<RegistryIdentifier> {
    val separatorIndex = value.indexOf(Identifier.NAMESPACE_SEPARATOR)
    val isTag = value.startsWith('#')

    val searchPath: String
    val filterPredicate: (Identifier) -> Boolean

    if (separatorIndex == -1) {
        searchPath = value.drop(if (isTag) 1 else 0)

        filterPredicate = {
            it.path.contains(value, ignoreCase = true) || MiscUtil.getFromRegistry(this, it)
                .asItem().name.string.contains(value, ignoreCase = true)
        }
    } else {
        val namespace = value.substring(if (isTag) 1 else 0, separatorIndex)
        searchPath = value.substring(separatorIndex + 1)

        filterPredicate = { it.namespace == namespace && it.path.startsWith(searchPath, ignoreCase = true) }
    }

    val idsSequence = streamTags().asSequence().filter { filterPredicate(it.id) }
        .map { RegistryIdentifier(it.id, isTag = true) } + if (!isTag) {
        ids.asSequence().filter(filterPredicate).map { RegistryIdentifier(it, isTag = false) }
    } else emptySequence()

    return idsSequence.sortedWith(compareBy<RegistryIdentifier> {
        !it.value.path.startsWith(searchPath, ignoreCase = true)
    }.thenBy(RegistryIdentifier::value).then { l, r -> l.isTag.compareTo(r.isTag) })
}

operator fun DefaultedRegistry<*>.contains(id: RegistryIdentifier) = if (id.isTag) {
    val tagsIterator = streamTags().iterator()
    !tagsIterator.hasNext() || tagsIterator.asSequence().any { it.id == id.value }
} else containsId(id.value)

fun <T : ItemConvertible> DefaultedRegistry<T>.find(identifier: String): T = find(RegistryIdentifier.of(identifier))

fun <T> DefaultedRegistry<T>.find(id: RegistryIdentifier): T {
    return if (id.isTag) {
        val tag = streamTags().asSequence().find { it.id == id.value } ?: return this[defaultId]

        //? if <=1.21.1
        getEntryList(tag).orElse(null)?.firstOrNull()?.value() ?: this[defaultId]
        //? if >=1.21.2
        /*tag.firstOrNull()?.value() ?: this[defaultId]*/
    } else this[id.value]
}
