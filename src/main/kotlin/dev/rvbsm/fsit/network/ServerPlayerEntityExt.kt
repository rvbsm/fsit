package dev.rvbsm.fsit.network

import dev.rvbsm.fsit.api.ConfigurableEntity
import dev.rvbsm.fsit.api.Crawlable
import dev.rvbsm.fsit.api.Poseable
import dev.rvbsm.fsit.config.ModConfig
import dev.rvbsm.fsit.entity.CrawlEntity
import dev.rvbsm.fsit.entity.Pose
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d

internal fun <T> ServerPlayerEntity.sendIfPossible(packet: T, orAction: () -> Unit = {}) where T : FabricPacket {
    if (ServerPlayNetworking.canSend(this, packet.type)) {
        ServerPlayNetworking.send(this, packet)
    } else orAction()
}

// todo: prevent mid-air posing 🤨?
fun ServerPlayerEntity.setPose(pose: Pose, pos: Vec3d? = null) = (this as Poseable).`fsit$setPose`(pose, pos)
fun ServerPlayerEntity.resetPose() = (this as Poseable).`fsit$resetPose`()
fun ServerPlayerEntity.isInPose() = (this as Poseable).`fsit$isInPose`()

fun ServerPlayerEntity.setCrawl(crawlEntity: CrawlEntity) = (this as Crawlable).`fsit$startCrawling`(crawlEntity)
fun ServerPlayerEntity.removeCrawl() = (this as Crawlable).`fsit$stopCrawling`()
fun ServerPlayerEntity.hasCrawl() = (this as Crawlable).`fsit$isCrawling`()

fun ServerPlayerEntity.setConfig(config: ModConfig) = (this as ConfigurableEntity).`fsit$setConfig`(config)
fun ServerPlayerEntity.getConfig() = (this as ConfigurableEntity).`fsit$getConfig`()
fun ServerPlayerEntity.hasConfig() = (this as ConfigurableEntity).`fsit$hasConfig`()
