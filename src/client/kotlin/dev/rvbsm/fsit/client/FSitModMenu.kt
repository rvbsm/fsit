package dev.rvbsm.fsit.client

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.isxander.yacl3.api.ListOption
import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.dsl.YetAnotherConfigLib
import dev.isxander.yacl3.dsl.addDefaultText
import dev.isxander.yacl3.dsl.binding
import dev.isxander.yacl3.dsl.controller
import dev.isxander.yacl3.dsl.descriptionBuilder
import dev.isxander.yacl3.dsl.enumSwitch
import dev.isxander.yacl3.dsl.slider
import dev.isxander.yacl3.dsl.tickBox
import dev.rvbsm.fsit.FSitMod
import dev.rvbsm.fsit.client.gui.controller.RegistryController
import dev.rvbsm.fsit.client.option.KeyBindingMode
import dev.rvbsm.fsit.config.ModConfig
import dev.rvbsm.fsit.config.Sitting
import dev.rvbsm.fsit.registry.RegistryIdentifier
import dev.rvbsm.fsit.registry.toRegistrySet
import dev.rvbsm.fsit.util.text.translatable
import kotlinx.coroutines.launch
import net.minecraft.registry.Registries

// todo: make it look better 👽
object FSitModMenu : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { screen ->
            YetAnotherConfigLib(FSitMod.MOD_ID) {
                val configBuilder = ModConfig.Builder(FSitMod.config)

                val general by categories.registering {
                    val useServer by rootOptions.registering {
                        controller = tickBox()
                        binding(configBuilder::useServer, ModConfig.Default.useServer)
                        descriptionBuilder { addDefaultText() }
                    }

                    val sitting by groups.registering {
                        descriptionBuilder { addDefaultText() }

                        // todo: do i need to add some visuals?
                        val behaviour by options.registering {
                            controller = enumSwitch<Sitting.Behaviour>()
                            binding(configBuilder::sittingBehaviour, ModConfig.Default.sitting.behaviour)
                            descriptionBuilder { addDefaultText() }
                        }

                        val shouldCenter by options.registering {
                            controller = tickBox()
                            binding(configBuilder::sittingShouldCenter, ModConfig.Default.sitting.shouldCenter)
                            descriptionBuilder { addDefaultText() }
                        }
                    }

                    val keybindings by groups.registering {
                        descriptionBuilder { addDefaultText() }

                        val sitMode by options.registering {
                            controller = enumSwitch<KeyBindingMode>()
                            binding(
                                KeyBindingMode.Hybrid,
                                FSitModClient.sitMode::getValue,
                                FSitModClient.sitMode::setValue,
                            )
                            descriptionBuilder { addDefaultText() }
                        }

                        val crawlMode by options.registering {
                            controller = enumSwitch<KeyBindingMode>()
                            binding(
                                KeyBindingMode.Hybrid,
                                FSitModClient.crawlMode::getValue,
                                FSitModClient.crawlMode::setValue,
                            )
                            descriptionBuilder { addDefaultText() }
                        }
                    }
                }

                val onUse by categories.registering {
                    val sitting by rootOptions.registering {
                        controller = tickBox()
                        binding(configBuilder::onUseSitting, ModConfig.Default.onUse.sitting)
                        descriptionBuilder { addDefaultText() }
                    }
                    val riding by rootOptions.registering {
                        controller = tickBox()
                        binding(configBuilder::onUseRiding, ModConfig.Default.onUse.riding)
                        descriptionBuilder { addDefaultText() }
                    }
                    val range by rootOptions.registering {
                        controller = slider(range = 1..4L)
                        binding(configBuilder::onUseRange, ModConfig.Default.onUse.range)
                        descriptionBuilder { addDefaultText() }
                    }
                    val checkSuffocation by rootOptions.registering {
                        controller = tickBox()
                        binding(configBuilder::onUseCheckSuffocation, ModConfig.Default.onUse.checkSuffocation)
                        descriptionBuilder { addDefaultText() }
                    }

                    groups.register(
                        "blocks",
                        ListOption.createBuilder<RegistryIdentifier>()
                            .name("$categoryKey.root.option.blocks".translatable()).description(
                                OptionDescription.createBuilder()
                                    .apply { addDefaultText("$categoryKey.root.option.blocks.description") }.build()
                            ).description(
                                OptionDescription.createBuilder().apply {
                                    addDefaultText("$categoryKey.root.option.blocks.description")
                                }.build()
                            ).customController { RegistryController(it, Registries.BLOCK) }.binding(
                                ModConfig.Default.onUse.blocks.toList(),
                                configBuilder.onUseBlocks::toList,
                            ) { configBuilder.onUseBlocks = it.toRegistrySet(Registries.BLOCK) }
                            .initial(RegistryIdentifier.defaultId).build())
                }

                val onSneak by categories.registering {
                    val sitting by rootOptions.registering {
                        controller = tickBox()
                        binding(configBuilder::onSneakSitting, ModConfig.Default.onSneak.sitting)
                        descriptionBuilder { addDefaultText() }
                    }
                    val crawling by rootOptions.registering {
                        controller = tickBox()
                        binding(configBuilder::onSneakCrawling, ModConfig.Default.onSneak.crawling)
                        descriptionBuilder { addDefaultText() }
                    }
                    val minPitch by rootOptions.registering {
                        controller = slider(range = -90.0..90.0)
                        binding(configBuilder::onSneakMinPitch, ModConfig.Default.onSneak.minPitch)
                        descriptionBuilder { addDefaultText() }
                    }
                    val delay by rootOptions.registering {
                        controller = slider(range = 100..2000L)
                        binding(configBuilder::onSneakDelay, ModConfig.Default.onSneak.delay)
                        descriptionBuilder { addDefaultText() }
                    }
                }

                save {
                    modClientScope.launch {
                        FSitMod.writeConfig(configBuilder.build())
                        FSitModClient.syncConfig()
                    }
                }
            }.generateScreen(screen)
        }
    }
}
