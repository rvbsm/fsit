package dev.rvbsm.fsit.client.gui.screen

import com.terraformersmc.modmenu.api.ConfigScreenFactory
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
import dev.rvbsm.fsit.client.FSitModClient
import dev.rvbsm.fsit.client.gui.controller.RegistryController
import dev.rvbsm.fsit.client.modClientScope
import dev.rvbsm.fsit.client.option.KeyBindingMode
import dev.rvbsm.fsit.config.ModConfig
import dev.rvbsm.fsit.config.Sitting
import dev.rvbsm.fsit.registry.RegistryIdentifier
import dev.rvbsm.fsit.registry.toRegistrySet
import dev.rvbsm.fsit.util.text.translatable
import kotlinx.coroutines.launch
import net.minecraft.registry.Registries

val YACLScreenFactory = ConfigScreenFactory { parent ->
    YetAnotherConfigLib(FSitMod.MOD_ID) {
        val configBuilder = ModConfig.Builder(FSitMod.config)

        categories.register("general") {
            rootOptions.register("useServer") {
                controller = tickBox()
                binding(configBuilder::useServer, ModConfig.Default.useServer)
                descriptionBuilder { addDefaultText() }
            }

            groups.register("sitting") {
                descriptionBuilder { addDefaultText() }

                // todo: do i need to add some visuals?
                options.register("behaviour") {
                    controller = enumSwitch<Sitting.Behaviour>()
                    binding(configBuilder::sittingBehaviour, ModConfig.Default.sitting.behaviour)
                    descriptionBuilder { addDefaultText() }
                }

                options.register("shouldCenter") {
                    controller = tickBox()
                    binding(configBuilder::sittingShouldCenter, ModConfig.Default.sitting.shouldCenter)
                    descriptionBuilder { addDefaultText() }
                }
            }

            groups.register("keybindings") {
                descriptionBuilder { addDefaultText() }

                options.register("sitMode") {
                    controller = enumSwitch<KeyBindingMode>()
                    binding(
                        KeyBindingMode.Hybrid,
                        FSitModClient.sitMode::getValue,
                        FSitModClient.sitMode::setValue,
                    )
                    descriptionBuilder { addDefaultText() }
                }

                options.register("crawlMode") {
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

        categories.register("onUse") {
            rootOptions.register("sitting") {
                controller = tickBox()
                binding(configBuilder::onUseSitting, ModConfig.Default.onUse.sitting)
                descriptionBuilder { addDefaultText() }
            }
            rootOptions.register("riding") {
                controller = tickBox()
                binding(configBuilder::onUseRiding, ModConfig.Default.onUse.riding)
                descriptionBuilder { addDefaultText() }
            }
            rootOptions.register("range") {
                controller = slider(range = 1..4L)
                binding(configBuilder::onUseRange, ModConfig.Default.onUse.range)
                descriptionBuilder { addDefaultText() }
            }
            rootOptions.register("checkSuffocation") {
                controller = tickBox()
                binding(configBuilder::onUseCheckSuffocation, ModConfig.Default.onUse.checkSuffocation)
                descriptionBuilder { addDefaultText() }
            }

            groups.register(
                "blocks",
                ListOption.createBuilder<RegistryIdentifier>().name("$categoryKey.root.option.blocks".translatable())
                    .description(
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

        categories.register("onSneak") {
            rootOptions.register("sitting") {
                controller = tickBox()
                binding(configBuilder::onSneakSitting, ModConfig.Default.onSneak.sitting)
                descriptionBuilder { addDefaultText() }
            }
            rootOptions.register("crawling") {
                controller = tickBox()
                binding(configBuilder::onSneakCrawling, ModConfig.Default.onSneak.crawling)
                descriptionBuilder { addDefaultText() }
            }
            rootOptions.register("minPitch") {
                controller = slider(range = -90.0..90.0)
                binding(configBuilder::onSneakMinPitch, ModConfig.Default.onSneak.minPitch)
                descriptionBuilder { addDefaultText() }
            }
            rootOptions.register("delay") {
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
    }.generateScreen(parent)
}
