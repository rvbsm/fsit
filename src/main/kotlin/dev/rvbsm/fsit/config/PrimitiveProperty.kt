package dev.rvbsm.fsit.config

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlScalar
import kotlinx.serialization.json.*
import kotlin.reflect.KMutableProperty

typealias MutablePropertyProvider<T> = (ModConfig) -> KMutableProperty<T>

internal sealed class PrimitiveProperty<T>(
    private val key: String,
    private val propertyProvider: MutablePropertyProvider<T>,
    private val fromYaml: (YamlScalar) -> T,
    private val fromJson: (JsonPrimitive) -> T,
    internal var config: ModConfig? = null,
) : MigratedField {
    private val property: KMutableProperty<T> get() = propertyProvider(config!!)

    override fun toString() = key
    internal fun set(value: T) = property.setter.call(value)

    override fun migrate(yamlMap: YamlMap): Boolean {
        checkNotNull(config) { "Config must be provided" }

        val keys = key.split('.')
        var node = yamlMap
        keys.dropLast(1).forEach { node = node[it] ?: return false }

        val value = fromYaml(node[keys.last()] ?: return false)
        set(value)

        return true
    }

    override fun migrate(jsonObject: JsonObject) {
        checkNotNull(config) { "Config must be provided" }

        val keys = key.split('.')
        var node = jsonObject
        keys.dropLast(1).forEach { node = node[it] as? JsonObject ?: return }

        val value = fromJson(node[keys.last()] as? JsonPrimitive ?: return)
        set(value)
    }
}

internal class BooleanProperty(key: String, propertyProvider: MutablePropertyProvider<Boolean>) :
    PrimitiveProperty<Boolean>(key, propertyProvider, YamlScalar::toBoolean, JsonPrimitive::boolean)

internal class LongProperty(key: String, propertyProvider: MutablePropertyProvider<Long>) :
    PrimitiveProperty<Long>(key, propertyProvider, YamlScalar::toLong, JsonPrimitive::long)

internal class DoubleProperty(key: String, propertyProvider: MutablePropertyProvider<Double>) :
    PrimitiveProperty<Double>(key, propertyProvider, YamlScalar::toDouble, JsonPrimitive::double)
