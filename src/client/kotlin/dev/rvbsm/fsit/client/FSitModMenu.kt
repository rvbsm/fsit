package dev.rvbsm.fsit.client

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.isxander.yacl3.api.*
import dev.isxander.yacl3.api.controller.ControllerBuilder
import dev.isxander.yacl3.api.controller.LongSliderControllerBuilder
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder
import dev.rvbsm.fsit.FSitMod
import dev.rvbsm.fsit.client.controller.RegistryControllerBuilder
import dev.rvbsm.fsit.client.controller.RegistryHelper
import dev.rvbsm.fsit.config.*
import dev.rvbsm.fsit.config.container.*
import dev.rvbsm.fsit.util.literal
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.item.ItemConvertible
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.TagKey
import org.slf4j.LoggerFactory
import kotlin.reflect.KMutableProperty

// todo: make it look better 👽
object FSitModMenu : ModMenuApi {
    private val logger = LoggerFactory.getLogger(FSitModMenu::class.java)

    private inline fun <reified T : Any> optionBuilder(
        path: String, noinline controller: (Option<T>) -> ControllerBuilder<T>, field: KMutableProperty<T>, default: T
    ): Option.Builder<T> {
        val name = FSitMod.translatable("option", path)
        val desc = FSitMod.translatable("description", path)

        return Option.createBuilder<T>().name(name).description(OptionDescription.of(desc)).controller(controller)
            .binding(default, field.getter::call, field.setter::call)
    }

    private fun booleanOption(path: String, field: KMutableProperty<Boolean>, default: Boolean) =
        optionBuilder(path, TickBoxControllerBuilder::create, field, default).build()

    private fun longOption(path: String, field: KMutableProperty<Long>, default: Long, range: LongRange = 1..4L) =
        optionBuilder(
            path,
            { LongSliderControllerBuilder.create(it).range(range.first, range.last).step(range.step) },
            field,
            default
        ).build()

    private fun optionGroup(path: String, vararg options: Option<*>): OptionGroup {
        val name = FSitMod.translatable("group", path)
        return OptionGroup.createBuilder().name(name).apply { options.forEach { option(it) } }.build()
    }

    private fun <T : Any> listOptionBuilder(
        path: String,
        controller: (Option<T>) -> ControllerBuilder<T>,
        getter: () -> List<T>,
        setter: (List<T>) -> Unit,
        default: List<T>,
        initial: T,
    ): ListOption.Builder<T> {
        val name = FSitMod.translatable("option", path)
        val desc = FSitMod.translatable("description", path)
        return ListOption.createBuilder<T>().name(name).description(OptionDescription.of(desc)).controller(controller)
            .binding(default, getter, setter).initial(initial)
    }

    private inline fun <T : ItemConvertible, W : Any, reified C : Container, reified E : C> containerOption(
        path: String,
        containers: MutableSet<C>,
        noinline getConverter: (Iterable<C>) -> List<W>,
        noinline setConverter: (List<W>) -> Iterable<E>,
        default: List<W>,
        initial: W,
        registryHelper: RegistryHelper<T, W>,
    ) = listOptionBuilder(
        path,
        { RegistryControllerBuilder(it, registryHelper) },
        { getConverter(containers) },
        { containers.updateWith<C, E>(setConverter(it)) },
        default,
        initial,
    ).build()

    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { screen ->
            YetAnotherConfigLib.createBuilder().title("FSit".literal()).category(
                ConfigCategory.createBuilder().name("Main".literal()).option(
                    booleanOption("use_server", FSitMod.config::useServer, ModConfig.default.useServer)
                ).group(
                    optionGroup(
                        "sittable",
                        booleanOption(
                            "sittable.enabled",
                            FSitMod.config.sitting.onUse::enabled,
                            ModConfig.default.sitting.onUse.enabled
                        ),
                        longOption(
                            "sittable.radius",
                            FSitMod.config.sitting.onUse::range,
                            ModConfig.default.sitting.onUse.range
                        ),
                    )
                ).group(
                    containerOption(
                        "sittable.blocks",
                        FSitMod.config.sitting.onUse.blocks,
                        Iterable<BlockContainer>::getEntries,
                        Iterable<Block>::asEntries,
                        ModConfig.default.sitting.onUse.blocks.getEntries(),
                        Blocks.AIR,
                        RegistryHelper.Simple(Registries.BLOCK),
                    )
                ).group(
                    containerOption(
                        "sittable.tags",
                        FSitMod.config.sitting.onUse.blocks,
                        Iterable<BlockContainer>::getTags,
                        Iterable<TagKey<Block>>::asTags,
                        ModConfig.default.sitting.onUse.blocks.getTags(),
                        BlockTags.SLABS,
                        RegistryHelper.Tag(Registries.BLOCK),
                    )
                ).group(
                    optionGroup(
                        "riding",
                        booleanOption(
                            "riding.enabled",
                            FSitMod.config.riding.onUse::enabled,
                            ModConfig.default.riding.onUse.enabled
                        ),
                        longOption(
                            "riding.radius",
                            FSitMod.config.riding.onUse::range,
                            ModConfig.default.riding.onUse.range
                        ),
                    )
                ).build()
            ).save(FSitModClient::saveConfig).build().generateScreen(screen)
        }
    }
}
