package dev.rvbsm.fsit.client.option

import net.minecraft.util.TranslatableOption

enum class KeyBindingMode(private val translationKey: String) : TranslatableOption {
    Toggle("options.key.toggle"), Hold("options.key.hold"), Hybrid("option.fsit.key.hybrid");

    override fun getId() = ordinal
    override fun getTranslationKey() = translationKey
}
