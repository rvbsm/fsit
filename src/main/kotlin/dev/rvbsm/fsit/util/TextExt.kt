package dev.rvbsm.fsit.util

import net.minecraft.text.MutableText
import net.minecraft.text.Text

fun String.translatable(vararg args: Any): MutableText = Text.translatable(this, args)
fun String.literal(): MutableText = Text.literal(this)
