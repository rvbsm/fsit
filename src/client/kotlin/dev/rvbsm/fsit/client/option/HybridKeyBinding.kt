package dev.rvbsm.fsit.client.option

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import kotlin.time.Duration
import kotlin.time.TimeSource

@Environment(EnvType.CLIENT)
class HybridKeyBinding(
    id: String,
    code: Int,
    category: String,
    private val duration: Duration,
    private val modeGetter: () -> KeyBindingMode,
) : KeyBinding(id, InputUtil.Type.KEYSYM, code, category) {
    private val timeSource = TimeSource.Monotonic
    private var pressMark = timeSource.markNow()
    private var prevPressed = false
    private var prevSticky = false

    override fun setPressed(pressed: Boolean) {
        val isSticky = when (modeGetter()) {
            KeyBindingMode.Toggle -> true
            KeyBindingMode.Hold -> false
            KeyBindingMode.Hybrid -> (!pressed.also {
                if (pressed) pressMark = timeSource.markNow()
            } && prevPressed && timeSource.markNow() - pressMark <= duration) || prevSticky
        }

        if (isSticky && pressed) {
            super.setPressed(!isPressed)
        } else if (!isSticky) {
            super.setPressed(pressed)
        }

        prevPressed = pressed
        prevSticky = isSticky
    }

    internal fun untoggle() {
        super.setPressed(false)
    }
}
