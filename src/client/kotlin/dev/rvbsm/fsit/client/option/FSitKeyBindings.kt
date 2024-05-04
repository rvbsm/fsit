package dev.rvbsm.fsit.client.option

import dev.rvbsm.fsit.FSitMod
import dev.rvbsm.fsit.client.FSitModClient
import dev.rvbsm.fsit.client.network.pose
import dev.rvbsm.fsit.client.network.setPose
import dev.rvbsm.fsit.entity.PlayerPose
import dev.rvbsm.fsit.network.packet.PoseRequestC2SPayload
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import org.lwjgl.glfw.GLFW

object FSitKeyBindings : ClientTickEvents.EndTick {
    private val sitKey = HybridKeyBinding(
        "key.fsit.sit", GLFW.GLFW_KEY_RIGHT_CONTROL, KeyBinding.MISC_CATEGORY, 20,
    ) { FSitModClient.sitKeyMode.value }.apply(KeyBindingHelper::registerKeyBinding)
    private val crawlKey = HybridKeyBinding(
        "key.fsit.crawl", GLFW.GLFW_KEY_RIGHT_ALT, KeyBinding.MISC_CATEGORY, 20,
    ) { FSitModClient.crawlKeyMode.value }.apply(KeyBindingHelper::registerKeyBinding)

    private var wasPoseUpdatedFromKeybinding = false

    // note: idk what is happening here 💀
    override fun onEndTick(client: MinecraftClient) {
        if (!FSitModClient.isServerFSitCompatible) return

        if (sitKey.isPressed && crawlKey.isPressed) {
            return reset()
        }

        val player = client.player ?: return
        val currentPose = player.pose()

        val canSitMidAir = FSitMod.config.sitting.allowInAir || player.isOnGround
        if (currentPose == PlayerPose.Standing && (player.hasVehicle() || !canSitMidAir)) return

        val pose = when {
            sitKey.isPressed -> PlayerPose.Sitting
            crawlKey.isPressed -> PlayerPose.Crawling
            wasPoseUpdatedFromKeybinding -> PlayerPose.Standing
            else -> currentPose
        }
        wasPoseUpdatedFromKeybinding = sitKey.isPressed || crawlKey.isPressed

        if (pose != currentPose) {
            player.setPose(pose)
            FSitModClient.sendIfPossible(PoseRequestC2SPayload(pose))
        }
    }

    fun reset() {
        sitKey.untoggle()
        crawlKey.untoggle()
    }
}
