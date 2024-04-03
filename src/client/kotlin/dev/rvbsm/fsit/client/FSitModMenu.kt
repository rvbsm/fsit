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

    private inline fun <T : ItemConvertible, W : Any, reified M : Material> materialOption(
        path: String,
        materials: MutableSet<Material>,
        noinline getConverter: (Iterable<Material>) -> List<W>,
        noinline setConverter: (List<W>) -> Iterable<M>,
        default: List<W>,
        initial: W,
        registryHelper: RegistryHelper<T, W>,
    ) = listOptionBuilder(
        path,
        { RegistryControllerBuilder(it, registryHelper) },
        { getConverter(materials) },
        { materials.updateWith<M>(setConverter(it)) },
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
                            "sittable.enabled", FSitMod.config.sittable::enabled, ModConfig.default.sittable.enabled
                        ),
                        longOption(
                            "sittable.radius", FSitMod.config.sittable::radius, ModConfig.default.sittable.radius
                        ),
                    )
                ).group(
                    materialOption(
                        "sittable.blocks",
                        FSitMod.config.sittable.materials,
                        Iterable<Material>::getBlocks,
                        Iterable<Block>::asBlockEntries,
                        ModConfig.default.sittable.materials.getBlocks(),
                        Blocks.AIR,
                        RegistryHelper.Simple(Registries.BLOCK),
                    )
                ).group(
                    materialOption(
                        "sittable.tags",
                        FSitMod.config.sittable.materials,
                        Iterable<Material>::getTags,
                        Iterable<TagKey<Block>>::asTagEntries,
                        ModConfig.default.sittable.materials.getTags(),
                        BlockTags.SLABS,
                        RegistryHelper.Tag(Registries.BLOCK),
                    )
                ).group(
                    optionGroup(
                        "riding",
                        booleanOption(
                            "riding.enabled", FSitMod.config.riding::enabled, ModConfig.default.riding.enabled
                        ),
                        longOption("riding.radius", FSitMod.config.riding::radius, ModConfig.default.riding.radius),
                    )
                ).build()
            ).save(FSitModClient::saveConfig).build().generateScreen(screen)
        }
    }
}
