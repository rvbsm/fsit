package dev.rvbsm.fsit.registry

import dev.rvbsm.fsit.util.DEFAULT_IDENTIFIER
import dev.rvbsm.fsit.util.id
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.util.Identifier

@Serializable(RegistryIdentifier.Serializer::class)
data class RegistryIdentifier(val value: Identifier, val isTag: Boolean) {

    override fun toString() = buildString {
        if (isTag) append('#')
        append(value)
    }

    companion object {
        val defaultId = RegistryIdentifier(value = DEFAULT_IDENTIFIER, isTag = false)

        fun of(string: String): RegistryIdentifier {
            val isTag = string.startsWith('#')
            val id = string.drop(if (isTag) 1 else 0).id()

            return RegistryIdentifier(id, isTag)
        }
    }

    object Serializer : KSerializer<RegistryIdentifier> {
        override val descriptor = PrimitiveSerialDescriptor("dev.rvbsm.RegistryIdentifier", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: RegistryIdentifier) = encoder.encodeString("$value")
        override fun deserialize(decoder: Decoder) = of(decoder.decodeString())
    }
}

fun Collection<RegistryIdentifier>.filterNotDefault() = filterNot { it.value == DEFAULT_IDENTIFIER }
