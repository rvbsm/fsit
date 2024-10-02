package dev.rvbsm.fsit.util

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

internal fun Collection<JsonElement>.join() = if (size == 1) first()
else when (first()) {
    is JsonPrimitive -> JsonPrimitive(joinToString(separator = "") { (it as? JsonPrimitive)?.content ?: "" })

    is JsonArray -> JsonArray(flatMap { it as? JsonArray ?: listOf() })

    is JsonObject -> JsonObject(fold(mutableMapOf()) { acc, obj ->
        acc.apply { putAll(obj as? JsonObject ?: mapOf()) }
    })
}

internal operator fun JsonObject.plus(json: JsonObject) = JsonObject(this as Map<String, JsonElement> + json)

internal operator fun JsonObject.plus(pair: Pair<String, JsonElement>) =
    JsonObject(this as Map<String, JsonElement> + pair)
