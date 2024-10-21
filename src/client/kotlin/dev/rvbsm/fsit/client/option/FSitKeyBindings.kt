package dev.rvbsm.fsit.client.option

import dev.rvbsm.fsit.FSitMod
import dev.rvbsm.fsit.client.FSitModClient
import dev.rvbsm.fsit.client.networking.pose
import dev.rvbsm.fsit.client.networking.setPose
import dev.rvbsm.fsit.entity.PlayerPose
import dev.rvbsm.fsit.networking.payload.PoseRequestC2SPayload
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import org.lwjgl.glfw.GLFW
import kotlin.time.Duration.Companion.milliseconds

object FSitKeyBindings : ClientTickEvents.EndTick {
    private val sitKey = HybridKeyBinding(
        "key.fsit.sit",
        GLFW.GLFW_KEY_RIGHT_CONTROL,
        KeyBinding.MISC_CATEGORY,
        500.milliseconds,
        FSitModClient.sitMode::getValue,
    )
    private val crawlKey = HybridKeyBinding(
        "key.fsit.crawl",
        GLFW.GLFW_KEY_RIGHT_ALT,
        KeyBinding.MISC_CATEGORY,
        500.milliseconds,
        FSitModClient.crawlMode::getValue,
    )

    private var wasPoseUpdatedFromKeybinding = false

    internal fun register() {
        KeyBindingHelper.registerKeyBinding(sitKey)
        KeyBindingHelper.registerKeyBinding(crawlKey)

        ClientTickEvents.END_CLIENT_TICK.register(this)
    }

    // note: idk what is happening here 💀
    override fun onEndTick(client: MinecraftClient) {
        if (!FSitModClient.isServerFSitCompatible) return

        if (sitKey.isPressed && crawlKey.isPressed) {
            return reset()
        }

        val player = client.player ?: return
        val currentPose = player.pose()

        val canStartSitting = !FSitMod.config.sitting.behaviour.shouldDiscard || player.isOnGround
        if (currentPose == PlayerPose.Standing && (player.hasVehicle() || !canStartSitting)) return

        val pose = when {
            sitKey.isPressed -> PlayerPose.Sitting
            crawlKey.isPressed -> PlayerPose.Crawling
            wasPoseUpdatedFromKeybinding -> PlayerPose.Standing
            else -> currentPose
        }
        wasPoseUpdatedFromKeybinding = sitKey.isPressed || crawlKey.isPressed

        if (pose != currentPose) {
            player.setPose(pose)
            FSitModClient.trySend(PoseRequestC2SPayload(pose))
        }
    }

    @JvmStatic
    fun reset() {
        sitKey.untoggle()
        crawlKey.untoggle()
    }
}
