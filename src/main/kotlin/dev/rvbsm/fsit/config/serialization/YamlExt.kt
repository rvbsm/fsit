package dev.rvbsm.fsit.config.serialization

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlScalar
import com.charleskorn.kaml.yamlMap
import kotlinx.serialization.serializer

inline fun <reified T> Yaml.decodeFromYamlNode(node: YamlNode): T =
    decodeFromYamlNode(serializersModule.serializer(), node)

internal fun Collection<YamlNode>.joinToYamlNode() = if (isEmpty()) null
else if (size == 1) first()
else when (val firstElement = first()) {
    is YamlScalar -> firstElement.copy(joinToString(separator = "") { (it as? YamlScalar)?.content ?: "" })
    is YamlList -> firstElement.copy(map { (it as? YamlList)?.items ?: listOf() }.flatten())

    is YamlMap -> firstElement.copy(fold(mutableMapOf()) { acc, obj ->
        acc.apply { putAll((obj as? YamlMap)?.entries ?: mapOf()) }
    })

    else -> null
}

internal operator fun YamlMap.plus(yaml: YamlMap): YamlMap = copy(entries = yamlMap.entries + yaml.entries)
internal operator fun YamlMap.plus(pair: Pair<YamlScalar, YamlNode>): YamlMap = copy(entries = yamlMap.entries + pair)
