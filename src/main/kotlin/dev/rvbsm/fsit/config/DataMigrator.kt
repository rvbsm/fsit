package dev.rvbsm.fsit.config

import com.charleskorn.kaml.*
import dev.rvbsm.fsit.util.splitOnce
import kotlinx.serialization.json.*

// todo: make another refactor after a while
private const val PATH_SEPARATOR = '.'
private const val FORCE_MIGRATION = '!'

private const val COMBINE_OPERATOR = '+'

private const val PREFIX_MODIFIER = '^'
private const val UPDATE_MODIFIER = '='
private val MODIFIERS = arrayOf(PREFIX_MODIFIER, UPDATE_MODIFIER).toCharArray()

internal fun JsonObject.migrate(migrations: Map<String, String>): JsonObject {
    if (migrations.isEmpty()) return this

    var jsonObjectModified = jsonObject
    val migratedOptions = buildMap(migrations.size) {
        for (migration in migrations) {
            val element = migration.key.split(COMBINE_OPERATOR).mapNotNull { fromKey ->
                val (fromPath, modifiers) = fromKey.splitOnce(*MODIFIERS)
                val fromKeys = fromPath.split(PATH_SEPARATOR)

                fromKeys.fold<String, JsonElement?>(jsonObjectModified) { acc, key -> (acc as? JsonObject)?.get(key) }
                    .also { jsonObjectModified = jsonObjectModified.removePath(fromPath) }?.applyModifiers(modifiers)
            }.takeIf { it.isNotEmpty() }?.join() ?: continue

            val toPath = migration.value.takeIf { it.endsWith(FORCE_MIGRATION) }?.dropLast(1)
                ?.also { jsonObjectModified = jsonObjectModified.removePath(it) } ?: migration.value

            this[toPath] = element
        }
    }

    return migratedOptions.asJsonObject() + jsonObjectModified
}

private fun Collection<JsonElement>.join() = if (size == 1) first()
else when (first()) {
    is JsonPrimitive -> JsonPrimitive(joinToString(separator = "") { (it as? JsonPrimitive)?.content ?: "" })

    is JsonArray -> JsonArray(flatMap { it as? JsonArray ?: listOf() })

    is JsonObject -> JsonObject(fold(mutableMapOf()) { acc, obj ->
        acc.apply { putAll(obj as? JsonObject ?: mapOf()) }
    })
}

internal fun YamlMap.migrate(migrations: Map<String, String>): YamlMap {
    if (migrations.isEmpty()) return this

    var yamlMapModified = yamlMap
    val migratedOptions = buildMap(migrations.size) {
        for (migration in migrations) {
            val node = migration.key.split(COMBINE_OPERATOR).mapNotNull { fromKey ->
                val (fromPath, modifiers) = fromKey.splitOnce(*MODIFIERS)
                val fromKeys = fromPath.split(PATH_SEPARATOR)

                fromKeys.fold<String, YamlNode?>(yamlMapModified) { acc, key -> (acc as? YamlMap)?.get(key) }
                    .also { yamlMapModified = yamlMapModified.removePath(fromPath) }?.applyModifiers(modifiers)
            }.takeIf { it.isNotEmpty() }?.join() ?: continue

            val toPath = migration.value.takeIf { it.endsWith(FORCE_MIGRATION) }?.dropLast(1)
                ?.also { yamlMapModified = yamlMapModified.removePath(it) } ?: migration.value

            this[toPath] = node
        }
    }

    return migratedOptions.asYamlMap(yamlMapModified) + yamlMapModified
}

private fun Collection<YamlNode>.join() = if (size == 1) first()
else when (val firstElement = first()) {
    is YamlScalar -> firstElement.copy(joinToString(separator = "") { (it as? YamlScalar)?.content ?: "" })
    is YamlList -> firstElement.copy(map { (it as? YamlList)?.items ?: listOf() }.flatten())

    is YamlMap -> firstElement.copy(fold(mutableMapOf()) { acc, obj ->
        acc.apply { putAll((obj as? YamlMap)?.entries ?: mapOf()) }
    })

    else -> null
}

private fun JsonObject.removePath(path: String): JsonObject {
    val (key, valuePath) = path.splitOnce(PATH_SEPARATOR)

    if (valuePath.isEmpty()) return JsonObject(filterKeys { it != key })
    else {
        val obj = (get(key) as? JsonObject)?.removePath(valuePath) ?: return this
        if (obj.isEmpty()) return JsonObject(this - key)

        return JsonObject(this + (key to obj))
    }
}

private fun YamlMap.removePath(path: String): YamlMap {
    val (key, valuePath) = path.splitOnce(PATH_SEPARATOR)

    if (valuePath.isEmpty()) return copy(entries.filterKeys { it.content != key })
    else {
        val map = (get(key) as? YamlMap)?.removePath(valuePath) ?: return this
        val mapKey = getKey(key)!!
        if (map.entries.isEmpty()) return copy(entries - mapKey)

        return copy(entries + (mapKey to map))
    }
}

private fun Map<String, JsonElement>.asJsonObject(): JsonObject = JsonObject(buildMap {
    this@asJsonObject.forEach { (path, element) ->
        val (key, elementPath) = path.splitOnce(PATH_SEPARATOR)

        this[key] = if (elementPath.isNotEmpty()) getOrDefault(
            key,
            JsonObject(mapOf()),
        ).jsonObject + mapOf(elementPath to element).asJsonObject()
        else element
    }
})

private fun Map<String, YamlNode>.asYamlMap(originalMap: YamlMap?): YamlMap = YamlMap(buildMap {
    this@asYamlMap.forEach { (path, node) ->
        val (key, nodePath) = path.splitOnce(PATH_SEPARATOR)
        val scalarKey = originalMap?.getKey(key) ?: YamlScalar(key, YamlPath.root)
        val originalNode = originalMap?.get<YamlNode>(key)

        this[scalarKey] = if (nodePath.isNotEmpty()) getOrDefault(
            scalarKey,
            originalNode as? YamlMap ?: YamlMap(mapOf(), YamlPath.root),
        ).yamlMap + mapOf(nodePath to node).asYamlMap(originalNode as? YamlMap)
        else originalNode ?: node
    }
}, originalMap?.path ?: YamlPath.root)

private operator fun JsonObject.plus(json: JsonObject) = JsonObject(this as Map<String, JsonElement> + json)
private operator fun YamlMap.plus(yaml: YamlMap): YamlMap = copy(entries = yamlMap.entries + yaml.entries)

// todo: i wrote this crap at 3 am
private fun String.applyModifiers(modifiers: String) = buildString {
    append(this@applyModifiers)

    fun Char.modifierValue(valueConsumer: (String) -> Unit) {
        val modifierIndex = modifiers.indexOf(this) + 1
        if (modifierIndex > 0 && modifierIndex <= modifiers.lastIndex) {
            val indices = modifierIndex..<modifiers.indexOfAny(MODIFIERS, modifierIndex)
            valueConsumer(modifiers.slice(indices))
        }
    }

    UPDATE_MODIFIER.modifierValue { clear().append(it) }
    PREFIX_MODIFIER.modifierValue { insert(0, it) }
}

private fun JsonElement.applyModifiers(modifiers: String): JsonElement {
    if (modifiers.isBlank()) return this

    fun JsonPrimitive.applyModifiers(modifiers: String) = JsonPrimitive(content.applyModifiers(modifiers))
    fun JsonArray.applyModifiers(modifiers: String) = JsonArray(map { it.jsonPrimitive.applyModifiers(modifiers) })

    return when (this) {
        is JsonPrimitive -> applyModifiers(modifiers)
        is JsonArray -> applyModifiers(modifiers)

        else -> this
    }
}

private fun YamlNode.applyModifiers(modifiers: String): YamlNode {
    if (modifiers.isBlank()) return this

    fun YamlScalar.applyModifiers(modifiers: String) = copy(content = content.applyModifiers(modifiers))
    fun YamlList.applyModifiers(modifiers: String) = copy(items = items.map { it.yamlScalar.applyModifiers(modifiers) })

    return when (this) {
        is YamlScalar -> applyModifiers(modifiers)
        is YamlList -> applyModifiers(modifiers)

        else -> this
    }
}
