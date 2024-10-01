package dev.rvbsm.fsit.client.event

import dev.rvbsm.fsit.FSitMod
import dev.rvbsm.fsit.client.FSitModClient
import dev.rvbsm.fsit.networking.payload.ConfigUpdateC2SPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents

val ClientConnectionListener = ClientPlayConnectionEvents.Join { _, _, _ ->
    FSitModClient.trySend(ConfigUpdateC2SPayload(FSitMod.config))
}
