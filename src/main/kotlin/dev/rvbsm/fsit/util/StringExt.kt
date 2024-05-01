package dev.rvbsm.fsit.util

import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Identifier

fun String.id(namespace: String = "") = if (namespace.isNotEmpty()) {
    Identifier.tryParse(namespace + Identifier.NAMESPACE_SEPARATOR + lowercase())
} else Identifier.tryParse(lowercase())

fun String.translatable(vararg args: Any): MutableText = Text.translatable(this, args)
fun String.literal(): MutableText = Text.literal(this)

fun String.lowercaseFirst() = replaceFirstChar { it.lowercase() }
