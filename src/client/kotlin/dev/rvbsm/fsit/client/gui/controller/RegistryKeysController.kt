package dev.rvbsm.fsit.client.gui.controller

import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.utils.Dimension
import dev.isxander.yacl3.gui.YACLScreen
import dev.isxander.yacl3.gui.controllers.dropdown.AbstractDropdownController
import dev.isxander.yacl3.gui.controllers.dropdown.AbstractDropdownControllerElement
import dev.rvbsm.fsit.registry.find
import dev.rvbsm.fsit.registry.matchingIdentifiers
import dev.rvbsm.fsit.util.id
import dev.rvbsm.fsit.util.literal
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.registry.DefaultedRegistry
import net.minecraft.text.Text

class RegistryController<T>(option: Option<String>, internal val registry: DefaultedRegistry<T>) :
    AbstractDropdownController<String>(option) where T : ItemConvertible {
    override fun getString() = option.pendingValue()
    override fun setFromString(value: String) = option.requestSet(value)
    override fun formatValue() = string.literal()

    override fun isValueValid(value: String) = if (value.startsWith('#')) {
        value.drop(1).id()
            .let { id -> registry.streamTags().findAny().isEmpty || registry.streamTags().anyMatch { it.id == id } }
    } else registry.containsId(value.id())

    override fun getValidValue(value: String, offset: Int) =
        registry.matchingIdentifiers(value).drop(offset).firstOrNull() ?: string

    override fun provideWidget(screen: YACLScreen, widgetDimension: Dimension<Int>) =
        RegistryControllerElement(this, screen, widgetDimension)
}

class RegistryControllerElement<T>(
    private val controller: RegistryController<T>, screen: YACLScreen, dim: Dimension<Int>
) : AbstractDropdownControllerElement<String, String>(controller, screen, dim) where T : ItemConvertible {
    private var current: T? = null
    private val matching = mutableMapOf<String, T>()

    override fun drawValueText(graphics: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val oldDimension = dimension
        dimension = dimension.withWidth(dimension.width() - decorationPadding)
        super.drawValueText(graphics, mouseX, mouseY, delta)
        dimension = oldDimension
        current?.let {
            graphics.drawItemWithoutEntity(
                ItemStack(it), dimension.xLimit() - xPadding - decorationPadding + 2, dimension.y() + 2
            )
        }
    }

    override fun computeMatchingValues(): List<String> {
        val ids = controller.registry.matchingIdentifiers(inputField).toList()
        current = controller.registry.find(inputField)

        matching.clear()
        matching.putAll(ids.associateWith { (controller.registry.find(it) ?: controller.registry[it.id()]) })

        return ids
    }

    override fun renderDropdownEntry(graphics: DrawContext, entryDimension: Dimension<Int>, string: String) {
        super.renderDropdownEntry(graphics, entryDimension, string)
        graphics.drawItemWithoutEntity(
            ItemStack(matching[string]),
            entryDimension.xLimit() - 2,
            entryDimension.y() + 1,
        )
    }

    override fun getString(string: String) = string

    override fun getDecorationPadding() = 16
    override fun getDropdownEntryPadding() = 4
    override fun getControlWidth() = super.getControlWidth() + decorationPadding

    override fun getValueText(): Text = when {
        inputField.isEmpty() -> super.getValueText()
        inputFieldFocused -> Text.literal(inputField)
        !inputField.startsWith('#') && current is ItemConvertible -> (current as ItemConvertible).asItem().name

        else -> inputField.literal()
    }
}
