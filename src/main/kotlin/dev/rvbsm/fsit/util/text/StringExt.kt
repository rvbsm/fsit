package dev.rvbsm.fsit.util.text

fun String.lowercaseFirst() = replaceFirstChar { it.lowercase() }

fun CharSequence.splitOnce(vararg delimiters: Char): Pair<String, String> =
    split(*delimiters, limit = 2).let { it[0] to it.getOrNull(1).orEmpty() }
