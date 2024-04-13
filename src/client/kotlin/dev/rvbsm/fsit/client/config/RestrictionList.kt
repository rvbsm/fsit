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
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

private val restrictionConfigPath = FabricLoader.getInstance().configDir.resolve("$MOD_ID.restrictions.json")

object RestrictionList {
    private val restrictedPlayers = mutableSetOf<@Serializable(UUIDSerializer::class) UUID>()

    @JvmStatic
    fun add(uuid: UUID) = restrictedPlayers.add(uuid).also { _ ->
        save()

        MinecraftClient.getInstance().player?.takeIf { player ->
            player.hasPassenger { it.uuid == uuid }
        }?.also { FSitModClient.sendIfPossible(RidingResponseC2SPayload(uuid, false)) }
    }

    @JvmStatic
    fun remove(uuid: UUID) = restrictedPlayers.remove(uuid).also { save() }

    @JvmStatic
    fun isRestricted(uuid: UUID) = restrictedPlayers.contains(uuid)

    private fun save() = restrictionConfigPath.writeText(Json.encodeToString(restrictedPlayers))

    fun load() {
        if (restrictionConfigPath.exists()) {
            restrictedPlayers.addAll(Json.decodeFromString(restrictionConfigPath.readText()))
        } else save()
    }
}
