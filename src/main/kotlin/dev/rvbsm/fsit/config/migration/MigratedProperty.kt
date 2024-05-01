package dev.rvbsm.fsit.config.migration

import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlScalar
import com.charleskorn.kaml.yamlScalar
import dev.rvbsm.fsit.config.ModConfig
import dev.rvbsm.fsit.util.RegistrySet
import dev.rvbsm.fsit.util.registrySetOf
import kotlinx.serialization.json.*
import kotlin.reflect.KMutableProperty

typealias MutablePropertyProvider<T> = (ModConfig) -> KMutableProperty<T>

internal sealed class MigratedProperty<T>(
    override val key: String,
    private val propertyProvider: MutablePropertyProvider<T>,
    internal var config: ModConfig? = null,
) : MigratedOption {
    internal val property: KMutableProperty<T> get() = propertyProvider(config!!)

    internal fun set(value: T) = property.setter.call(value)

    override fun migrate(yamlMap: YamlMap) {
        checkNotNull(config) { "Config must be provided" }

        val keys = key.split('.')
        var node = yamlMap
        keys.dropLast(1).forEach { node = node[it] ?: return }

        val value = parseValue(node, keys.last()) ?: return
        set(value)
    }

    override fun migrate(jsonObject: JsonObject) {
        checkNotNull(config) { "Config must be provided" }

        val keys = key.split('.')
        var node = jsonObject
        keys.dropLast(1).forEach { node = node[it] as? JsonObject ?: return }

        val value = parseValue(node, keys.last()) ?: return
        set(value)
    }

    abstract fun parseValue(node: YamlMap, key: String): T?
    abstract fun parseValue(node: JsonObject, key: String): T?
}

internal sealed class PrimitiveProperty<T>(
    key: String,
    propertyProvider: MutablePropertyProvider<T>,
    private val fromYaml: (YamlScalar) -> T,
    private val fromJson: (JsonPrimitive) -> T,
) : MigratedProperty<T>(key, propertyProvider) {

    override fun parseValue(node: YamlMap, key: String): T? {
        return fromYaml(node[key] ?: return null)
    }

    override fun parseValue(node: JsonObject, key: String): T? {
        return fromJson(node[key] as? JsonPrimitive ?: return null)
    }
}

internal class BooleanProperty(key: String, propertyProvider: MutablePropertyProvider<Boolean>) :
    PrimitiveProperty<Boolean>(key, propertyProvider, YamlScalar::toBoolean, JsonPrimitive::boolean)

internal class LongProperty(key: String, propertyProvider: MutablePropertyProvider<Long>) :
    PrimitiveProperty<Long>(key, propertyProvider, YamlScalar::toLong, JsonPrimitive::long)

internal class DoubleProperty(key: String, propertyProvider: MutablePropertyProvider<Double>) :
    PrimitiveProperty<Double>(key, propertyProvider, YamlScalar::toDouble, JsonPrimitive::double)

internal class RegistrySetProperty<T>(
    key: String,
    propertyProvider: MutablePropertyProvider<RegistrySet<T>>,
    private val type: Type,
) : MigratedProperty<RegistrySet<T>>(key, propertyProvider) {

    private val registry get() = property.getter.call().registry

    override fun parseValue(node: YamlMap, key: String): RegistrySet<T>? {
        val ids = node.get<YamlList>(key)?.items?.map { type.prefix + it.yamlScalar.content } ?: return null
        return registrySetOf(registry, *ids.toTypedArray())
    }

    override fun parseValue(node: JsonObject, key: String): RegistrySet<T>? {
        val ids = node[key]?.jsonArray?.map { type.prefix + it.jsonPrimitive.content } ?: return null
        return registrySetOf(registry, *ids.toTypedArray())
    }

    enum class Type(val prefix: String = "") {
        CONTAINER, ENTRIES, TAGS("#");
    }
}
