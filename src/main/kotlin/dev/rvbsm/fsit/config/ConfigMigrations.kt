package dev.rvbsm.fsit.config

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlScalar
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

private const val ANCIENT_VERSION = -2
private const val LEGACY_VERSION = -1

private val ancientMigrations by lazy {
    mapOf(
        "version=$LEGACY_VERSION"           to "version!",

        "sneak.enabled"                     to "on_double_sneak.sitting",
        "sneak.min_angle"                   to "on_double_sneak.min_pitch",
        "sneak.delay"                       to "on_double_sneak.delay",

        "misc.ride_players"                 to "on_use.riding",

        "misc.riding.enabled"               to "on_use.riding",

        "sneak.angle"                       to "on_double_sneak.min_pitch",

        "sittable.blocks+sittable.tags^#"   to "on_use.blocks",
    )
}

private val legacyMigrations by lazy {
    mapOf(
        "version=0"                         to "version!",

        "sittable.enabled"                  to "on_use.sitting",
        "sittable.radius"                   to "on_use.range",
        "sittable.materials"                to "on_use.blocks",
        "riding.enabled"                    to "on_use.riding",

        "sitting.seats_gravity"             to "sitting.apply_gravity",
        "sitting.on_use.enabled"            to "on_use.sitting",
        "sitting.on_use.range"              to "on_use.range",
        "sitting.on_use.blocks"             to "on_use.blocks",
        "sitting.on_double_sneak.enabled"   to "on_double_sneak.sitting",
        "sitting.on_double_sneak.min_pitch" to "on_double_sneak.min_pitch",
        "sitting.on_double_sneak.delay"     to "on_double_sneak.delay",
        "riding.on_use.enabled"             to "on_use.riding",
    )
}

private val v0Migrations by lazy {
    mapOf(
        "version=1"                         to "version!"
    )
}

private fun getMigrations(version: Int = 0) = buildMap {
    if (version <= ANCIENT_VERSION) putAll(ancientMigrations)
    if (version <= LEGACY_VERSION) putAll(legacyMigrations)

    if (version <= 0) putAll(v0Migrations)
}

private val JsonObject.version get() =
    get("version")?.runCatching { jsonPrimitive.intOrNull }?.getOrNull()
        ?: if ("config_version" in this) ANCIENT_VERSION else LEGACY_VERSION

private val YamlMap.version get() =
    getScalar("version")?.runCatching(YamlScalar::toInt)?.getOrNull()
        ?: if (getKey("config_version") != null) ANCIENT_VERSION else LEGACY_VERSION

internal val JsonObject.migrations get() = getMigrations(version)
internal val YamlMap.migrations get() = getMigrations(version)
