package dev.rvbsm.fsit.client

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.isxander.yacl3.api.ListOption
import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.dsl.*
import dev.rvbsm.fsit.FSitMod
import dev.rvbsm.fsit.client.gui.controller.RegistryController
import dev.rvbsm.fsit.config.ModConfig
import dev.rvbsm.fsit.registry.toRegistrySet
import dev.rvbsm.fsit.util.translatable
import net.minecraft.registry.Registries

// todo: make it look better 👽
object FSitModMenu : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { screen ->
            YetAnotherConfigLib(FSitMod.MOD_ID) {
                val general by categories.registering {
                    val useServer by rootOptions.registering {
                        controller = tickBox()
                        binding(FSitMod.config::useServer, ModConfig.default.useServer)
                        descriptionBuilder { addDefaultText(lines = 1) }
                    }

                    val sitting by groups.registering {
                        descriptionBuilder { addDefaultText(lines = 1) }

                        val applyGravity by options.registering {
                            controller = tickBox()
                            binding(FSitMod.config.sitting::applyGravity, ModConfig.default.sitting.applyGravity)
                            descriptionBuilder { addDefaultText(lines = 1) }
                        }
                        val allowInAir by options.registering {
                            controller = tickBox()
                            binding(FSitMod.config.sitting::allowInAir, ModConfig.default.sitting.allowInAir)
                            descriptionBuilder { addDefaultText(lines = 1) }
                        }
                    }

                    val riding by groups.registering {
                        descriptionBuilder { addDefaultText(lines = 1) }

                        val hideRider by options.registering {
                            controller = tickBox()
                            binding(FSitMod.config.riding::hideRider, ModConfig.default.riding.hideRider)
                            descriptionBuilder { addDefaultText(lines = 1) }
                        }
                    }
                }

                val onUse by categories.registering {
                    val sitting by rootOptions.registering {
                        controller = tickBox()
                        binding(FSitMod.config.onUse::sitting, ModConfig.default.onUse.sitting)
                        descriptionBuilder { addDefaultText(lines = 1) }
                    }
                    val riding by rootOptions.registering {
                        controller = tickBox()
                        binding(FSitMod.config.onUse::riding, ModConfig.default.onUse.riding)
                        descriptionBuilder { addDefaultText(lines = 1) }
                    }
                    val range by rootOptions.registering {
                        controller = slider(range = 1..4L)
                        binding(FSitMod.config.onUse::range, ModConfig.default.onUse.range)
                        descriptionBuilder { addDefaultText(lines = 1) }
                    }
                    val checkSuffocation by rootOptions.registering {
                        controller = tickBox()
                        binding(FSitMod.config.onUse::checkSuffocation, ModConfig.default.onUse.checkSuffocation)
                        descriptionBuilder { addDefaultText(lines = 1) }
                    }

                    groups.register(
                        "blocks",
                        ListOption.createBuilder<String>().name("$categoryKey.root.option.blocks".translatable())
                            .description(OptionDescription.of("$categoryKey.root.option.blocks.description".translatable()))
                            .customController { RegistryController(it, Registries.BLOCK) }.binding(
                                ModConfig.default.onUse.blocks.toList(),
                                { FSitMod.config.onUse.blocks.toList() },
                                { FSitMod.config.onUse.blocks = it.toRegistrySet(Registries.BLOCK) },
                            ).initial("#slabs").build()
                    )
                }

                val onSneak by categories.registering {
                    val sitting by rootOptions.registering {
                        controller = tickBox()
                        binding(FSitMod.config.onDoubleSneak::sitting, ModConfig.default.onDoubleSneak.sitting)
                        descriptionBuilder { addDefaultText(lines = 1) }
                    }
                    val riding by rootOptions.registering {
                        controller = tickBox()
                        binding(FSitMod.config.onDoubleSneak::crawling, ModConfig.default.onDoubleSneak.crawling)
                        descriptionBuilder { addDefaultText(lines = 1) }
                    }
                    val minPitch by rootOptions.registering {
                        controller = slider(range = -90.0..90.0)
                        binding(FSitMod.config.onDoubleSneak::minPitch, ModConfig.default.onDoubleSneak.minPitch)
                        descriptionBuilder { addDefaultText(lines = 1) }
                    }
                    val delay by rootOptions.registering {
                        controller = slider(range = 100..2000L)
                        binding(FSitMod.config.onDoubleSneak::delay, FSitMod.config.onDoubleSneak.delay)
                        descriptionBuilder { addDefaultText(lines = 1) }
                    }
                }

                save { FSitModClient.saveConfig() }
            }.generateScreen(screen)
        }
    }
}
