package dev.rvbsm.fsit.config.serialization

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

internal fun Collection<JsonElement>.joinToJsonElement() = if (isEmpty()) null
else if (size == 1) first()
else when (first()) {
    is JsonPrimitive -> JsonPrimitive(joinToString(separator = "") { (it as? JsonPrimitive)?.content ?: "" })

    is JsonArray -> JsonArray(flatMap { it as? JsonArray ?: listOf() })

    is JsonObject -> JsonObject(fold(mutableMapOf()) { acc, obj ->
        acc.apply { putAll(obj as? JsonObject ?: mapOf()) }
    })
}

private fun JsonObject.asMap(): Map<String, JsonElement> = this

internal operator fun JsonObject.plus(json: JsonObject) = JsonObject(content = asMap() + json)
internal operator fun JsonObject.plus(pair: Pair<String, JsonElement>) = JsonObject(content = asMap() + pair)
