package dev.rvbsm.fsit.client.option

import dev.rvbsm.fsit.client.FSitModClient
import dev.rvbsm.fsit.client.network.pose
import dev.rvbsm.fsit.client.network.setPose
import dev.rvbsm.fsit.entity.Pose
import dev.rvbsm.fsit.network.packet.PoseRequestC2SPacket
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.option.StickyKeyBinding
import org.lwjgl.glfw.GLFW

object FSitKeyBindings : ClientTickEvents.EndTick {
    private val sitKey = KeyBindingHelper.registerKeyBinding(StickyKeyBinding(
        "key.fsit.sit", GLFW.GLFW_KEY_RIGHT_CONTROL, KeyBinding.MISC_CATEGORY
    ) { FSitModClient.sitKeyMode.value.isSticky(holdTicks) })
    private val crawlKey = KeyBindingHelper.registerKeyBinding(StickyKeyBinding(
        "key.fsit.crawl", GLFW.GLFW_KEY_RIGHT_ALT, KeyBinding.MISC_CATEGORY
    ) { FSitModClient.crawlKeyMode.value.isSticky(holdTicks) })

    private var holdTicks = 0
    private var wasUpdatedFromKeybinding = false

    // note: idk what is happening here 💀
    override fun onEndTick(client: MinecraftClient) {
        if (!FSitModClient.isServerFSitCompatible) return

        val player = client.player ?: return
        val currentPose = player.pose()
        if (currentPose == Pose.Standing && player.hasVehicle()) return

        if ((sitKey.isPressed && crawlKey.isPressed) || player.abilities.flying || player.isSneaking) {
            sitKey.isPressed = sitKey.isPressed && FSitModClient.sitKeyMode.value.isSticky(holdTicks)
            crawlKey.isPressed = crawlKey.isPressed && FSitModClient.sitKeyMode.value.isSticky(holdTicks)
        }

        val pose = when {
            sitKey.isPressed -> Pose.Sitting
            crawlKey.isPressed -> Pose.Crawling
            wasUpdatedFromKeybinding -> Pose.Standing
            else -> currentPose
        }
        wasUpdatedFromKeybinding = sitKey.isPressed || crawlKey.isPressed

        when {
            wasUpdatedFromKeybinding -> holdTicks += 1
            pose == Pose.Standing && holdTicks > 0 -> holdTicks = 0
        }

        if (pose != currentPose) {
            player.setPose(pose)
            FSitModClient.sendIfPossible(PoseRequestC2SPacket(pose))
        }
    }
}
