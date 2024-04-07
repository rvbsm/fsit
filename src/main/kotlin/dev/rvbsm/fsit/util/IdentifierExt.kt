package dev.rvbsm.fsit.util

import net.minecraft.util.Identifier

fun Identifier.asString(): String = if (namespace == "minecraft") path else "$namespace:$path"
