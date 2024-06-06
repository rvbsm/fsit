package dev.rvbsm.fsit.util

import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Identifier

fun String.id(namespace: String = Identifier.DEFAULT_NAMESPACE): Identifier? =
    if (Identifier.NAMESPACE_SEPARATOR in this) Identifier.tryParse(lowercase())
    //? if <=1.20.6
    else Identifier.of(namespace, lowercase()) // nuh uh, we will throw an exception in 1.21+
    //? if >1.20.6
    /*else Identifier.tryParse(namespace, lowercase())*/

fun String.translatable(vararg args: Any): MutableText = Text.translatable(this, args)
fun String.literal(): MutableText = Text.literal(this)

fun String.lowercaseFirst() = replaceFirstChar { it.lowercase() }
