package dev.rvbsm.fsit.client.config

import dev.rvbsm.fsit.FSitMod.MOD_ID
import dev.rvbsm.fsit.client.FSitModClient
import dev.rvbsm.fsit.config.serialization.UUIDSerializer
import dev.rvbsm.fsit.network.packet.RidingResponseC2SPayload
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import java.util.*
import kotlin.io.path.readText
import kotlin.io.path.writeText

private val restrictionConfigPath = FabricLoader.getInstance().configDir.resolve("$MOD_ID.restrictions.json")

object RestrictionList {
    private lateinit var restrictedPlayers: MutableSet<@Serializable(UUIDSerializer::class) UUID>

    @JvmStatic
    fun add(uuid: UUID) = restrictedPlayers.add(uuid).also {
        trySendUpdate(uuid)
        save()
    }

    @JvmStatic
    fun remove(uuid: UUID) = restrictedPlayers.remove(uuid).also { save() }

    @JvmStatic
    fun isRestricted(uuid: UUID) = restrictedPlayers.contains(uuid)

    private fun trySendUpdate(uuid: UUID) {
        val player = MinecraftClient.getInstance().player ?: return
        if (player.hasPassenger { it.uuid == uuid }) {
            FSitModClient.trySend(RidingResponseC2SPayload(uuid, false))
        }
    }

    private fun save() = restrictionConfigPath.writeText(Json.encodeToString(restrictedPlayers))

    fun load() {
        restrictedPlayers = runCatching {
            Json.decodeFromString<MutableSet<UUID>>(restrictionConfigPath.readText())
        }.getOrElse { mutableSetOf() }

        save()
    }
}
