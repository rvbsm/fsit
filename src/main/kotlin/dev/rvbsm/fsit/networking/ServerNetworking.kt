package dev.rvbsm.fsit.networking

import dev.rvbsm.fsit.entity.RideEntity
import dev.rvbsm.fsit.modScope
import dev.rvbsm.fsit.networking.payload.ConfigUpdateC2SPayload
import dev.rvbsm.fsit.networking.payload.CustomPayload
import dev.rvbsm.fsit.networking.payload.PoseRequestC2SPayload
import dev.rvbsm.fsit.networking.payload.RidingResponseC2SPayload
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.server.network.ServerPlayerEntity

internal val ConfigUpdateC2SHandler = ServerPayloadHandler<ConfigUpdateC2SPayload> { player, _ ->
    modScope.launch {
        player.config = decode()
    }
}

internal val PoseRequestC2SHandler = ServerPayloadHandler<PoseRequestC2SPayload> { player, _ ->
    player.modPose = pose
}

internal val RidingResponseC2SHandler = ServerPayloadHandler<RidingResponseC2SPayload> { player, _ ->
    if (!response.isAccepted && player.hasPassenger { (it as? RideEntity)?.isBelongsTo(uuid) == true }) {
        player.removeAllPassengers()
    }

    player.completeRidingRequest(this)
}

internal fun interface ServerPayloadHandler<P : CustomPayload<P>> : //$ ServerPlayNetworking.PlayPayloadHandler >>
    net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPayloadHandler<P> {

    fun P.handle(player: ServerPlayerEntity, responseSender: PacketSender)

    //? if <=1.20.4 {
    /*override fun receive(packet: P, player: ServerPlayerEntity, responseSender: PacketSender) =
        packet.handle(player, responseSender)
    *///?} else if >=1.20.5 {
    override fun receive(payload: P, context: net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context) =
        payload.handle(context.player(), context.responseSender())
    //?}
}
