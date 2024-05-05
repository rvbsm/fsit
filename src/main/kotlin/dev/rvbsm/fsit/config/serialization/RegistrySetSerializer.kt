package dev.rvbsm.fsit.config.serialization

import dev.rvbsm.fsit.registry.RegistrySet
import dev.rvbsm.fsit.registry.registrySetOf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.registry.DefaultedRegistry
import net.minecraft.registry.Registries

internal sealed class RegistrySetSerializer<E>(private val registry: DefaultedRegistry<E>) : KSerializer<RegistrySet<E>> {
    private val setSerializer = SetSerializer(String.serializer())
    override val descriptor = setSerializer.descriptor

    override fun deserialize(decoder: Decoder): RegistrySet<E> {
        val ids = decoder.decodeSerializableValue(setSerializer)

        return registrySetOf(registry, *ids.toTypedArray())
    }

    override fun serialize(encoder: Encoder, value: RegistrySet<E>) {
        encoder.encodeSerializableValue(setSerializer, value)
    }

    internal object Block : RegistrySetSerializer<net.minecraft.block.Block>(Registries.BLOCK)
}
