package dev.rvbsm.fsit.client.networking

import dev.rvbsm.fsit.client.FSitModClient
import dev.rvbsm.fsit.client.event.untoggleKeyBindings
import dev.rvbsm.fsit.client.minecraftClient
import dev.rvbsm.fsit.networking.payload.CustomPayload
import dev.rvbsm.fsit.networking.payload.PoseUpdateS2CPayload
import dev.rvbsm.fsit.networking.payload.RidingRequestS2CPayload
import dev.rvbsm.fsit.networking.payload.RidingResponseC2SPayload
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.network.ClientPlayerEntity

internal val PoseUpdateS2CHandler = ClientPayloadHandler<PoseUpdateS2CPayload> { player, _ ->
    if (player.pose() != pose) {
        player.setPose(pose)
        untoggleKeyBindings()
    }
}

internal val RidingRequestS2CHandler = ClientPayloadHandler<RidingRequestS2CPayload> { _, responseSender ->
    val isHidden = minecraftClient.socialInteractionsManager.isPlayerHidden(playerUUID)
    val isBlocked = minecraftClient.socialInteractionsManager.isPlayerBlocked(playerUUID)

    val isRestricted = FSitModClient.isRestricted(playerUUID) || isHidden || isBlocked
    responseSender.sendPacket(RidingResponseC2SPayload(playerUUID, !isRestricted))
}

internal fun interface ClientPayloadHandler<P : CustomPayload<P>> : //$ ClientPlayNetworking.PlayPayloadHandler >>
    net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPacketHandler<P> {

    fun P.handle(player: ClientPlayerEntity, responseSender: PacketSender)

    //? if <=1.20.4 {
    override fun receive(packet: P, player: ClientPlayerEntity, responseSender: PacketSender) =
        packet.handle(player, responseSender)
    //?} else if >=1.20.5 {
    /*override fun receive(
        payload: P, context: net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context
    ) = payload.handle(context.player(), context.responseSender())
    *///?}
}
