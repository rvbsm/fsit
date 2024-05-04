package dev.rvbsm.fsit.client.option

import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil

class HybridKeyBinding(
    id: String,
    code: Int,
    category: String,
    private val durationTicks: Int,
    private val modeGetter: () -> KeyBindingMode,
) : KeyBinding(id, InputUtil.Type.KEYSYM, code, category) {
    private var pressTimeMillis = 0L
    private var isSticky = true
    private var wasPressed = false

    override fun setPressed(pressed: Boolean) {
        when (modeGetter()) {
            KeyBindingMode.Toggle -> isSticky = true
            KeyBindingMode.Hold -> isSticky = false
            KeyBindingMode.Hybrid -> {
                if (pressed) {
                    pressTimeMillis = System.currentTimeMillis()
                } else if (wasPressed) {
                    isSticky = (System.currentTimeMillis() - pressTimeMillis) / 50 < durationTicks
                }
                wasPressed = pressed
            }
        }

        if (isSticky) {
            if (pressed) {
                super.setPressed(!isPressed)
            }
        } else {
            super.setPressed(pressed)
        }
    }

    internal fun untoggle() {
        super.setPressed(false)
    }
}
