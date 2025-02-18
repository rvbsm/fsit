package dev.rvbsm.fsit.serialization.migration

import com.charleskorn.kaml.Location
import com.charleskorn.kaml.YamlInput
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlNull
import com.charleskorn.kaml.YamlPath
import com.charleskorn.kaml.YamlScalar
import com.charleskorn.kaml.YamlTaggedNode
import dev.rvbsm.fsit.serialization.YamlBuilder
import dev.rvbsm.fsit.serialization.preferContextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.overwriteWith
import kotlinx.serialization.modules.serializersModuleOf

class YamlProcessor(override val target: YamlMap) : MigrationProcessor<YamlNode> {
    override fun YamlNode.toMutable(): MutableNode = when (this) {
        is YamlNull -> MutableNodeNull

        is YamlScalar -> MutableNodeLiteral(content, isString = true)

        is YamlList -> MutableNodeList(items.mapTo(mutableListOf()) { it.toMutable() })

        is YamlMap -> MutableNodeMap(entries.mapKeys { (key) -> key.content }
            .mapValuesTo(mutableMapOf()) { (_, node) -> node.toMutable() })

        is YamlTaggedNode -> innerNode.toMutable()
    }

    override fun MutableNode.toImmutable(): YamlNode = toImmutable(YamlPath.root)

    private val nullLocation = Location(0, 0)

    // note: i'm not sure if path is correct tho
    private fun MutableNode.toImmutable(path: YamlPath): YamlNode = when (this) {
        is MutableNodeNull -> YamlNull(path)

        is MutableNodeLiteral -> YamlScalar(content, path)

        is MutableNodeList -> YamlList(mapIndexed { idx, node ->
            node.toImmutable(path.withListEntry(idx, nullLocation))
        }, path)

        is MutableNodeMap -> YamlMap(mapKeys { (key) ->
            YamlScalar(key, path.withMapElementKey(key, nullLocation))
        }.mapValues { (key, node) -> node.toImmutable(key.path.withMapElementValue(nullLocation)) }, path)
    }
}

abstract class YamlTransformingSerializer<T : Any>(private val tSerializer: KSerializer<T>) : KSerializer<T> {
    override val descriptor: SerialDescriptor get() = tSerializer.descriptor

    final override fun serialize(encoder: Encoder, value: T) {
        tSerializer.serialize(encoder, value)
    }

    final override fun deserialize(decoder: Decoder): T {
        val input = decoder as? YamlInput ?: error(
            "This serializer can be used only with Yaml format." +
                "Expected Decoder to be YamlInput, got ${decoder::class}"
        )
        val node = input.node
        return input.yaml.decodeFromYamlNode(tSerializer, transformDeserialize(node))
    }

    protected open fun transformDeserialize(node: YamlNode): YamlNode = node
}

class YamlMigrationSerializer<T : Any>(
    tSerializer: KSerializer<T>,
    schemas: Iterable<MigrationSchema>,
    val parseVersion: YamlMap.() -> Int,
) : YamlTransformingSerializer<T>(tSerializer) {

    private val sortedSchemas = schemas.sortedByVersion()

    override fun transformDeserialize(node: YamlNode): YamlNode = when (node) {
        !is YamlMap -> node
        else -> YamlProcessor(node) + sortedSchemas.forVersion(node.parseVersion())
    }
}

val YamlMap.version get() = runCatching { getScalar("version")?.toInt() }.getOrNull() ?: 0

inline fun <reified T : Any> YamlBuilder.withMigration(
    schemas: Iterable<MigrationSchema>,
    noinline versionProvider: YamlMap.() -> Int = YamlMap::version,
) {
    serializersModule = serializersModule.overwriteWith(
        serializersModuleOf(YamlMigrationSerializer(serializersModule.preferContextual<T>(), schemas, versionProvider))
    )
}
