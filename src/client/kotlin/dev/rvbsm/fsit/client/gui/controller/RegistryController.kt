package dev.rvbsm.fsit.client.gui.controller

import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.utils.Dimension
import dev.isxander.yacl3.gui.YACLScreen
import dev.isxander.yacl3.gui.controllers.dropdown.AbstractDropdownController
import dev.isxander.yacl3.gui.controllers.dropdown.AbstractDropdownControllerElement
import dev.rvbsm.fsit.registry.RegistryIdentifier
import dev.rvbsm.fsit.registry.contains
import dev.rvbsm.fsit.registry.find
import dev.rvbsm.fsit.registry.matchingIdentifiers
import dev.rvbsm.fsit.util.text.literal
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.registry.DefaultedRegistry
import net.minecraft.text.Text

class RegistryController<T : ItemConvertible>(
    option: Option<RegistryIdentifier>,
    internal val registry: DefaultedRegistry<T>,
) : AbstractDropdownController<RegistryIdentifier>(option) {

    override fun getString() = option.pendingValue().toString()
    override fun setFromString(value: String) = option.requestSet(RegistryIdentifier.of(value))
    override fun formatValue() = string.literal()
    override fun isValueValid(value: String) = option.pendingValue() in registry

    override fun getValidValue(value: String, offset: Int) =
        registry.matchingIdentifiers(value).drop(offset.coerceAtLeast(0)).firstOrNull()?.toString() ?: string

    override fun provideWidget(screen: YACLScreen, widgetDimension: Dimension<Int>) =
        RegistryControllerElement(this, screen, widgetDimension)
}

class RegistryControllerElement<T : ItemConvertible>(
    private val registryController: RegistryController<T>?,
    screen: YACLScreen,
    dim: Dimension<Int>,
) : AbstractDropdownControllerElement<RegistryIdentifier, RegistryIdentifier>(registryController, screen, dim) {
    private val registry = registryController!!.registry
    private var currentElement: T? = null
    private val matchingElements = mutableMapOf<RegistryIdentifier, T>()

    override fun drawValueText(graphics: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val prevDimension = dimension
        dimension = dimension.withWidth(dimension.width() - decorationPadding)
        super.drawValueText(graphics, mouseX, mouseY, delta)

        dimension = prevDimension
        currentElement?.let {
            graphics.drawItemWithoutEntity(
                ItemStack(it),
                dimension.xLimit() - xPadding - decorationPadding + 2,
                dimension.y() + 2,
            )
        }
    }

    override fun computeMatchingValues(): List<RegistryIdentifier> {
        val ids = registry.matchingIdentifiers(inputField).toList()
        currentElement = registry.find(inputField)

        matchingElements.clear()
        matchingElements.putAll(ids.associateWith(registry::find))

        return ids
    }

    override fun renderDropdownEntry(graphics: DrawContext, entryDimension: Dimension<Int>, value: RegistryIdentifier) {
        super.renderDropdownEntry(graphics, entryDimension, value)
        graphics.drawItemWithoutEntity(
            ItemStack(matchingElements[value]),
            entryDimension.xLimit() - 2,
            entryDimension.y() + 1,
        )
    }

    override fun getString(id: RegistryIdentifier) = id.toString()
    override fun getDecorationPadding() = 16
    override fun getDropdownEntryPadding() = 4
    override fun getControlWidth() = super.getControlWidth() + decorationPadding

    override fun getValueText(): Text = when {
        inputField.isEmpty() -> super.getValueText()
        inputFieldFocused -> inputField.literal()
        registryController?.option()?.pendingValue()?.isTag == false -> currentElement?.asItem()?.name
            ?: inputField.literal()

        else -> inputField.literal()
    }
}
