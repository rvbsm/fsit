package dev.rvbsm.fsit.client.network

import dev.rvbsm.fsit.client.config.RestrictionList
import dev.rvbsm.fsit.network.packet.PoseUpdateS2CPacket
import dev.rvbsm.fsit.network.packet.RidingRequestS2CPacket
import dev.rvbsm.fsit.network.packet.RidingResponseC2SPacket
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.network.ClientPlayerEntity
import org.slf4j.LoggerFactory

object FSitClientNetworking {
    private val logger = LoggerFactory.getLogger(FSitClientNetworking::class.java)

    fun register() {
        ClientPlayNetworking.registerGlobalReceiver(PoseUpdateS2CPacket.pType, ::onReceiveUpdatePose)
        ClientPlayNetworking.registerGlobalReceiver(RidingRequestS2CPacket.pType, ::onReceiveRequestRiding)
    }

    private fun onReceiveUpdatePose(packet: PoseUpdateS2CPacket, player: ClientPlayerEntity, sender: PacketSender) {
        player.setPose(packet.pose)
    }

    private fun onReceiveRequestRiding(
        packet: RidingRequestS2CPacket, player: ClientPlayerEntity, sender: PacketSender
    ) {
        sender.sendPacket(
            RidingResponseC2SPacket(
                packet.uuid, !RestrictionList.isRestricted(packet.uuid)
            )
        )
    }
}
