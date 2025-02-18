package dev.rvbsm.fsit.serialization.migration

sealed class MutableNode

sealed class MutableNodePrimitive(var content: String, val isString: Boolean) : MutableNode()
class MutableNodeList(content: MutableList<MutableNode>) : MutableNode(), MutableList<MutableNode> by content
class MutableNodeMap(content: MutableMap<String, MutableNode>) : MutableNode(),
    MutableMap<String, MutableNode> by content

class MutableNodeLiteral(content: String, isString: Boolean) : MutableNodePrimitive(content, isString)
data object MutableNodeNull : MutableNodePrimitive(content = "null", isString = false)

interface MigrationProcessor<OriginalNode> {
    val target: OriginalNode

    fun OriginalNode.toMutable(): MutableNode
    fun MutableNode.toImmutable(): OriginalNode

    fun Sequence<MutableNode>.joinToNodeOrNull(): MutableNode? = singleOrNull() ?: when (val node = firstOrNull()) {
        is MutableNodePrimitive -> node.apply {
            content = joinToString(separator = "") { (it as? MutableNodePrimitive)?.content.orEmpty() }
        }

        is MutableNodeList -> node.also {
            for (childNode in this) {
                node.addAll(childNode as? MutableNodeList ?: emptyList())
            }
        }

        is MutableNodeMap -> node.also {
            for (childNode in this) {
                node.putAll(childNode as? MutableNodeMap ?: emptyMap())
            }
        }

        else -> node
    }

    fun Iterable<MutableNode>.joinToNodeOrNull(): MutableNode? = asSequence().joinToNodeOrNull()

    fun MutableNode.transform(transformer: Transformer): MutableNode {
        when (this) {
            is MutableNodePrimitive -> content = transformer(content)
            is MutableNodeList -> for (node in this) node.transform(transformer)
            is MutableNodeMap -> for ((_, node) in this) node.transform(transformer)
        }

        return this
    }

    operator fun plus(migrationSchemas: Collection<MigrationSchema>): OriginalNode {
        if (migrationSchemas.isEmpty()) return target

        val mutableNode = target.toMutable() as MutableNodeMap

        for (schema in migrationSchemas) {
            for ((origins, destination) in schema.migrations) {
                val node = origins.mapNotNull { origin ->
                    val originKeys = origin.path.split('.')
                    originKeys.foldIndexed<String, MutableNode?>(mutableNode) { idx, acc, key ->
                        (acc as? MutableNodeMap)?.let {
                            if (idx == originKeys.lastIndex) it.remove(key) else it[key]
                        }
                    }?.transform(origin.modifier)
                }.joinToNodeOrNull()?.transform(destination.modifier) ?: continue

                val destinationKeys = destination.path.split('.')
                destinationKeys.dropLast(1).fold(mutableNode) { acc, key ->
                    acc[key] as? MutableNodeMap ?: MutableNodeMap(mutableMapOf()).also { acc[key] = it }
                }[destinationKeys.last()] = node
            }

            mutableNode["version"] = MutableNodeLiteral("${schema.version}", isString = false)
        }

        return mutableNode.toImmutable()
    }
}

internal fun Iterable<MigrationSchema>.sortedByVersion() = sortedBy { it.version }
internal fun List<MigrationSchema>.forVersion(version: Int) = takeLastWhile { it.version > version }
