package dev.rvbsm.fsit.client

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.isxander.yacl3.api.*
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder
import dev.isxander.yacl3.api.controller.LongSliderControllerBuilder
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder
import dev.rvbsm.fsit.FSitMod
import dev.rvbsm.fsit.client.gui.controller.RegistryController
import dev.rvbsm.fsit.client.gui.controller.RegistryHelper
import dev.rvbsm.fsit.config.ModConfig
import dev.rvbsm.fsit.config.container.*
import dev.rvbsm.fsit.util.literal
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.TagKey
import org.slf4j.LoggerFactory

// todo: make it look better 👽
object FSitModMenu : ModMenuApi {
    private val logger = LoggerFactory.getLogger(FSitModMenu::class.java)

    private val categoryGeneral = FSitMod.translatable("category", "general")
    private val categoryOnUse = FSitMod.translatable("category", "on_use")
    private val categoryOnDoubleSneak = FSitMod.translatable("category", "on_double_sneak")

    private val groupSitting = FSitMod.translatable("group", "sitting")
    private val groupRiding = FSitMod.translatable("group", "riding")
    private val descriptionSitting = FSitMod.translatable("description", "sitting")
    private val descriptionRiding = FSitMod.translatable("description", "riding")

    private val optionUseServer = FSitMod.translatable("option", "use_server")
    private val optionSittingSeatsGravity = FSitMod.translatable("option", "sitting.seats_gravity")
    private val optionSittingAllowMidAir = FSitMod.translatable("option", "sitting.allow_mid_air")
    private val optionRidingHideRider = FSitMod.translatable("option", "riding.hide_rider")
    private val optionOnUseSitting = FSitMod.translatable("option", "on_use.sitting")
    private val optionOnUseRiding = FSitMod.translatable("option", "on_use.riding")
    private val optionOnUseRange = FSitMod.translatable("option", "on_use.range")
    private val optionOnUseSuffocationCheck = FSitMod.translatable("option", "on_use.suffocation_check")
    private val optionOnUseBlocks = FSitMod.translatable("option", "on_use.blocks")
    private val optionOnUseTags = FSitMod.translatable("option", "on_use.tags")
    private val optionOnDoubleSneakSitting = FSitMod.translatable("option", "on_double_sneak.sitting")
    private val optionOnDoubleSneakCrawling = FSitMod.translatable("option", "on_double_sneak.crawling")
    private val optionOnDoubleSneakMinPitch = FSitMod.translatable("option", "on_double_sneak.min_pitch")
    private val optionOnDoubleSneakDelay = FSitMod.translatable("option", "on_double_sneak.delay")

    private val descriptionUseServer = FSitMod.translatable("description", "use_server")
    private val descriptionSittingSeatsGravity = FSitMod.translatable("description", "sitting.seats_gravity")
    private val descriptionSittingAllowMidAir = FSitMod.translatable("description", "sitting.allow_mid_air")
    private val descriptionRidingHideRider = FSitMod.translatable("description", "riding.hide_rider")
    private val descriptionOnUseSitting = FSitMod.translatable("description", "on_use.sitting")
    private val descriptionOnUseRiding = FSitMod.translatable("description", "on_use.riding")
    private val descriptionOnUseRange = FSitMod.translatable("description", "on_use.range")
    private val descriptionOnUseSuffocationCheck = FSitMod.translatable("description", "on_use.suffocation_check")
    private val descriptionOnUseBlocks = FSitMod.translatable("description", "on_use.blocks")
    private val descriptionOnUseTags = FSitMod.translatable("description", "on_use.tags")
    private val descriptionOnDoubleSneakSitting = FSitMod.translatable("description", "on_double_sneak.sitting")
    private val descriptionOnDoubleSneakCrawling = FSitMod.translatable("description", "on_double_sneak.crawling")
    private val descriptionOnDoubleSneakMinPitch = FSitMod.translatable("description", "on_double_sneak.min_pitch")
    private val descriptionOnDoubleSneakDelay = FSitMod.translatable("description", "on_double_sneak.delay")

    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { screen ->
            YetAnotherConfigLib.createBuilder().title("FSit".literal())
                .category(ConfigCategory.createBuilder().name(categoryGeneral)
                    .option(Option.createBuilder<Boolean>().name(optionUseServer)
                        .description { OptionDescription.createBuilder()
                            .text(descriptionUseServer)
                            .build()
                        }.controller(TickBoxControllerBuilder::create)
                        .binding(ModConfig.default.useServer,
                            { FSitMod.config.useServer },
                            { FSitMod.config.useServer = it }
                        ).build()
                    ).group(OptionGroup.createBuilder().name(groupSitting)
                        .description(OptionDescription.createBuilder()
                            .text(descriptionSitting)
                            .build()
                        ).options(listOf(
                            Option.createBuilder<Boolean>().name(optionSittingSeatsGravity)
                                .description { OptionDescription.createBuilder()
                                    .text(descriptionSittingSeatsGravity)
                                    .build()
                                }.controller(TickBoxControllerBuilder::create)
                                .binding(ModConfig.default.sitting.seatsGravity,
                                    { FSitMod.config.sitting.seatsGravity },
                                    { FSitMod.config.sitting.seatsGravity = it }
                                ).build(),
                            Option.createBuilder<Boolean>().name(optionSittingAllowMidAir)
                                .description { OptionDescription.createBuilder()
                                    .text(descriptionSittingAllowMidAir)
                                    .build()
                                }.controller(TickBoxControllerBuilder::create)
                                .binding(ModConfig.default.sitting.allowMidAir,
                                    { FSitMod.config.sitting.allowMidAir },
                                    { FSitMod.config.sitting.allowMidAir = it }
                                ).build()
                            )
                        ).build()
                    ).group(OptionGroup.createBuilder().name(groupRiding)
                        .description(OptionDescription.createBuilder()
                            .text(descriptionRiding)
                            .build()
                        ).options(listOf(
                            Option.createBuilder<Boolean>().name(optionRidingHideRider)
                                .description { OptionDescription.createBuilder()
                                    .text(descriptionRidingHideRider)
                                    .build()
                                }.controller(TickBoxControllerBuilder::create)
                                .binding(ModConfig.default.riding.hideRider,
                                    { FSitMod.config.riding.hideRider },
                                    { FSitMod.config.riding.hideRider = it }
                                ).build()
                            )
                        ).build()
                    ).build()
                ).category(ConfigCategory.createBuilder().name(categoryOnUse)
                    .options(listOf(
                        Option.createBuilder<Boolean>().name(optionOnUseSitting)
                            .description { OptionDescription.createBuilder()
                                .text(descriptionOnUseSitting)
                                .build()
                            }.controller(TickBoxControllerBuilder::create)
                            .binding(ModConfig.default.onUse.sitting,
                                { FSitMod.config.onUse.sitting },
                                { FSitMod.config.onUse.sitting = it }
                            ).build(),
                        Option.createBuilder<Boolean>().name(optionOnUseRiding)
                            .description { OptionDescription.createBuilder()
                                .text(descriptionOnUseRiding)
                                .build()
                            }.controller(TickBoxControllerBuilder::create)
                            .binding(ModConfig.default.onUse.riding,
                                { FSitMod.config.onUse.riding },
                                { FSitMod.config.onUse.riding = it }
                            ).build(),
                        Option.createBuilder<Long>().name(optionOnUseRange)
                            .description { OptionDescription.createBuilder()
                                .text(descriptionOnUseRange)
                                .build()
                            }.controller { LongSliderControllerBuilder.create(it).range(1, 4).step(1) }
                            .binding(ModConfig.default.onUse.range,
                                { FSitMod.config.onUse.range },
                                { FSitMod.config.onUse.range = it }
                            ).build(),
                        Option.createBuilder<Boolean>().name(optionOnUseSuffocationCheck)
                            .description { OptionDescription.createBuilder()
                                .text(descriptionOnUseSuffocationCheck)
                                .build()
                            }.controller(TickBoxControllerBuilder::create)
                            .binding(ModConfig.default.onUse.suffocationCheck,
                                { FSitMod.config.onUse.suffocationCheck },
                                { FSitMod.config.onUse.suffocationCheck = it }
                            ).build(),
                        )
                    ).group(ListOption.createBuilder<Block>().name(optionOnUseBlocks)
                        .description(OptionDescription.createBuilder()
                            .text(descriptionOnUseBlocks)
                            .build()
                        ).customController { RegistryController(it, RegistryHelper.Simple(Registries.BLOCK)) }
                        .binding(ModConfig.default.onUse.blocks.getEntries(),
                            FSitMod.config.onUse.blocks::getEntries
                        ) { FSitMod.config.onUse.blocks.updateWith(it.asEntries()) }.initial(Blocks.AIR)
                        .build()
                    ).group(ListOption.createBuilder<TagKey<Block>>().name(optionOnUseTags)
                        .description(OptionDescription.createBuilder()
                            .text(descriptionOnUseTags)
                            .build()
                        ).customController { RegistryController(it, RegistryHelper.Tag(Registries.BLOCK)) }
                        .binding(ModConfig.default.onUse.blocks.getTags(),
                            FSitMod.config.onUse.blocks::getTags
                        ) { FSitMod.config.onUse.blocks.updateWith(it.asTags()) }.initial(BlockTags.FENCES)
                        .build()
                    ).build()
                ).category(ConfigCategory.createBuilder().name(categoryOnDoubleSneak)
                    .options(listOf(
                        Option.createBuilder<Boolean>().name(optionOnDoubleSneakSitting)
                            .description { OptionDescription.createBuilder()
                                .text(descriptionOnDoubleSneakSitting)
                                .build()
                            }.controller(TickBoxControllerBuilder::create)
                            .binding(ModConfig.default.onDoubleSneak.sitting,
                                { FSitMod.config.onDoubleSneak.sitting },
                                { FSitMod.config.onDoubleSneak.sitting = it }
                            ).build(),
                        Option.createBuilder<Boolean>().name(optionOnDoubleSneakCrawling)
                            .description { OptionDescription.createBuilder()
                                .text(descriptionOnDoubleSneakCrawling)
                                .build()
                            }.controller(TickBoxControllerBuilder::create)
                            .binding(ModConfig.default.onDoubleSneak.crawling,
                                { FSitMod.config.onDoubleSneak.crawling },
                                { FSitMod.config.onDoubleSneak.crawling = it }
                            ).build(),
                        Option.createBuilder<Double>().name(optionOnDoubleSneakMinPitch)
                            .description { OptionDescription.createBuilder()
                                .text(descriptionOnDoubleSneakMinPitch)
                                .build()
                            }.controller { DoubleSliderControllerBuilder.create(it).range(-90.0, 90.0).step(1.0) }
                            .binding(ModConfig.default.onDoubleSneak.minPitch,
                                { FSitMod.config.onDoubleSneak.minPitch },
                                { FSitMod.config.onDoubleSneak.minPitch = it}
                            ).build(),
                        Option.createBuilder<Long>().name(optionOnDoubleSneakDelay)
                            .description { OptionDescription.createBuilder()
                                .text(descriptionOnDoubleSneakDelay)
                                .build()
                            }.controller { LongSliderControllerBuilder.create(it).range(100, 2000).step(25) }
                            .binding(ModConfig.default.onDoubleSneak.delay,
                                { FSitMod.config.onDoubleSneak.delay },
                                { FSitMod.config.onDoubleSneak.delay = it }
                            ).build()
                        )
                    ).build()
                ).save(FSitModClient::saveConfig).build().generateScreen(screen)
        }
    }
}
