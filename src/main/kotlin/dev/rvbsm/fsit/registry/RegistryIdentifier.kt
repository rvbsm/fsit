package dev.rvbsm.fsit.registry

import dev.rvbsm.fsit.util.DEFAULT_IDENTIFIER
import dev.rvbsm.fsit.util.id
import dev.rvbsm.fsit.util.orDefault
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.util.Identifier

@Serializable(RegistryIdentifier.Serializer::class)
data class RegistryIdentifier(val id: Identifier, val isTag: Boolean) {
    override fun toString() = if (isTag) "#$id" else "$id"

    companion object {
        fun of(id: String) = if (id.startsWith('#')) {
            RegistryIdentifier(id.drop(1).id().orDefault(), isTag = true)
        } else RegistryIdentifier(id.id().orDefault(), isTag = false)
    }

    object Serializer : KSerializer<RegistryIdentifier> {
        override val descriptor = PrimitiveSerialDescriptor("dev.rvbsm.RegistryIdentifier", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder) = of(decoder.decodeString())

        override fun serialize(encoder: Encoder, value: RegistryIdentifier) {
            encoder.encodeString("$value")
        }
    }
}

fun Collection<RegistryIdentifier>.filterNotDefault() = filterNot { it.id == DEFAULT_IDENTIFIER }
