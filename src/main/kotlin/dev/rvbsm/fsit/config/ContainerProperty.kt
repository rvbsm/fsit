package dev.rvbsm.fsit.config

import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlScalar
import dev.rvbsm.fsit.config.container.Container
import dev.rvbsm.fsit.util.id
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import net.minecraft.item.ItemConvertible
import net.minecraft.registry.tag.TagKey

internal class ContainerProperty<E : ItemConvertible, C : Container<E, *>>(
    private val key: String,
    private val type: Type,
    internal var container: C? = null,
) : MigratedField {
    override fun toString() = key

    override fun migrate(yamlMap: YamlMap): Boolean {
        checkNotNull(container) { "Container must be provided" }

        val keys = key.split('.')
        var node = yamlMap
        keys.dropLast(1).forEach { node = node[it] ?: return false }

        val yamlItems: YamlList = node[keys.last()] ?: return false
        val ids = yamlItems.items.map { it.yamlScalar.content }
        migrate(ids)

        return true
    }

    override fun migrate(jsonObject: JsonObject) {
        checkNotNull(container) { "Container must be provided" }

        val keys = key.split('.')
        var node = jsonObject
        keys.dropLast(1).forEach { node = node[it] as? JsonObject ?: return }

        val ids = node[keys.last()]?.jsonArray?.map { it.jsonPrimitive.content } ?: return
        migrate(ids)
    }

    private fun migrate(ids: Iterable<String>) = when (type) {
        Type.ENTRIES -> {
            val entries = ids.map { container!!.registry[it.id()] }
            container!!.updateEntries(entries)
        }

        Type.TAGS -> {
            val tags = ids.map { TagKey.of(container!!.registry.key, it.id()) }
            container!!.updateTags(tags)
        }

        Type.CONTAINER -> {
            val entries = ids.filter { !it.startsWith('#') }.map { container!!.registry[it.id()] }
            container!!.updateEntries(entries)
            val tags = ids.filter { it.startsWith('#') }.map { TagKey.of(container!!.registry.key, it.drop(1).id()) }
            container!!.updateTags(tags)
        }
    }

    enum class Type {
        CONTAINER, ENTRIES, TAGS
    }
}
