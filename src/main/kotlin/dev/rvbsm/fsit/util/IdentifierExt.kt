package dev.rvbsm.fsit.util

import net.minecraft.util.Identifier

val DEFAULT_IDENTIFIER: Identifier =
    //? if <=1.20.6
    Identifier("air")
    //? if >=1.21
    /*Identifier.ofVanilla("air")*/

fun Identifier?.orDefault(): Identifier = this ?: DEFAULT_IDENTIFIER

fun String.id(namespace: String = Identifier.DEFAULT_NAMESPACE) = runCatching {
    if (Identifier.NAMESPACE_SEPARATOR in this) Identifier.tryParse(lowercase())
    else Identifier.of(namespace, lowercase())
  }.getOrNull().orDefault()
