package dev.rvbsm.fsit.client.option

import dev.rvbsm.fsit.client.FSitModClient
import dev.rvbsm.fsit.client.network.pose
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
//        if (!FSitModClient.isServerFSitCompatible) return

        if (wasUpdatedFromKeybinding && sitKey.isPressed && crawlKey.isPressed) {
            sitKey.isPressed = FSitModClient.sitKeyMode.value.isSticky(holdTicks)
            crawlKey.isPressed = FSitModClient.crawlKeyMode.value.isSticky(holdTicks)
        }

        val player = client.player ?: return

        if (player.abilities.flying || player.isSneaking) {
            if (sitKey.isPressed) sitKey.isPressed = FSitModClient.sitKeyMode.value.isSticky(holdTicks)
            if (crawlKey.isPressed) crawlKey.isPressed = FSitModClient.crawlKeyMode.value.isSticky(holdTicks)
        }

        val pose = when {
            sitKey.isPressed -> Pose.Sitting
            crawlKey.isPressed -> Pose.Crawling
            wasUpdatedFromKeybinding -> Pose.Standing
            else -> player.pose()
        }
        wasUpdatedFromKeybinding = sitKey.isPressed || crawlKey.isPressed

        when {
            sitKey.isPressed || crawlKey.isPressed -> holdTicks += 1
            pose == Pose.Standing && holdTicks > 0 -> holdTicks = 0
        }

        if (pose != player.pose()) {
            FSitModClient.sendIfPossible(PoseRequestC2SPacket(pose))
        }
    }
}
