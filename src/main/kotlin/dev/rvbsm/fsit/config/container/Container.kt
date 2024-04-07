package dev.rvbsm.fsit.config.container

import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Encoder
import net.minecraft.text.Text

// todo
sealed interface Container/*<C, E, T> where C : Container<C, E, T>, E : C, T : C*/ {
    override fun toString(): String
    fun asText(): Text

    sealed interface Serializer<C : Container> : KSerializer<C> {
        override fun serialize(encoder: Encoder, value: C) {
            encoder.encodeString("$value")
        }
    }
}

inline fun <C : Container, reified E : C> MutableSet<C>.updateWith(containers: Iterable<E>) {
    removeAll { it is E }
    addAll(containers)
}
