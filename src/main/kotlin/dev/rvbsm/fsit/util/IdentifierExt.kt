package dev.rvbsm.fsit.util

import net.minecraft.util.Identifier

fun Identifier.asString(): String = if (namespace == "minecraft") path else "$namespace:$path"

fun String.id(namespace: String = Identifier.DEFAULT_NAMESPACE): Identifier? = runCatching {
    if (Identifier.NAMESPACE_SEPARATOR in this) Identifier.tryParse(lowercase())
    else Identifier.of(namespace, lowercase())
  }.getOrNull()
