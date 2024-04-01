package dev.rvbsm.fsit.client.option

import net.minecraft.util.TranslatableOption
import net.minecraft.util.function.ValueLists

enum class KeyBindingMode(private val translationKey: String) : TranslatableOption {
    Toggle("options.key.toggle"), Hold("options.key.hold"), Hybrid("option.fsit.key.hybrid");

    override fun getId() = ordinal
    override fun getTranslationKey() = translationKey

    fun isSticky(holdTicks: Int) = when (this) {
        Toggle -> true
        Hold -> false
        Hybrid -> holdTicks < 20
    }

    companion object {
        private val byId = ValueLists.createIdToValueFunction(
            KeyBindingMode::getId,
            enumValues<KeyBindingMode>(),
            ValueLists.OutOfBoundsHandling.WRAP
        )

        fun byId(id: Int): KeyBindingMode = byId.apply(id)
    }
}
