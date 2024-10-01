package dev.rvbsm.fsit.util

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlNode
import kotlinx.serialization.serializer

inline fun <reified T> Yaml.decodeFromYamlNode(node: YamlNode): T =
    decodeFromYamlNode(serializersModule.serializer(), node)
