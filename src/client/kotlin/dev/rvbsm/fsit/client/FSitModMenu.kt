package dev.rvbsm.fsit.client

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.rvbsm.fsit.client.gui.screen.YACLScreenFactory
import net.fabricmc.loader.api.FabricLoader

object FSitModMenu : ModMenuApi {

    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> = when {
        isModLoaded("yet_another_config_lib_v3") -> YACLScreenFactory

        else -> super.modConfigScreenFactory
    }

    private fun isModLoaded(id: String) = FabricLoader.getInstance().isModLoaded(id)
}
