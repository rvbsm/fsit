package dev.rvbsm.fsit.client.gui.controller

import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.utils.Dimension
import dev.isxander.yacl3.gui.AbstractWidget
import dev.isxander.yacl3.gui.YACLScreen
import dev.isxander.yacl3.gui.controllers.dropdown.AbstractDropdownController
import dev.isxander.yacl3.gui.controllers.dropdown.AbstractDropdownControllerElement
import dev.rvbsm.fsit.util.literal
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class RegistryController<T, W>(option: Option<W>, internal val registryHelper: RegistryHelper<T, W>) :
    AbstractDropdownController<W>(option) where T : ItemConvertible, W : Any {

    override fun getString() = registryHelper.toString(option.pendingValue())

    override fun setFromString(value: String) = option.requestSet(registryHelper.fromStringWrapper(value))

    override fun formatValue(): Text = Text.literal(string)

    override fun isValueValid(value: String) = registryHelper.isRegistered(value)

    override fun getValidValue(value: String, offset: Int) = registryHelper.validValue(value, offset.toLong()) ?: string

    override fun provideWidget(screen: YACLScreen, widgetDimension: Dimension<Int>): AbstractWidget =
        RegistryControllerElement(this, screen, widgetDimension)
}

class RegistryControllerElement<T, W>(
    control: RegistryController<T, W>,
    screen: YACLScreen,
    dim: Dimension<Int>,
    private val controller: RegistryController<T, W> = control
) : AbstractDropdownControllerElement<W, Identifier>(control, screen, dim) where T : ItemConvertible, W : Any {
    private var current: T? = null
    private val matching = mutableMapOf<Identifier, T>()

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

    override fun computeMatchingValues(): MutableList<Identifier> {
        val identifiers = controller.registryHelper.matchingIdentifiers(inputField).toList()
        current = controller.registryHelper.fromString(inputField)
        identifiers.forEach { matching[it] = controller.registryHelper.fromId(it) }

        return identifiers
    }

    /*? if <=1.20.4 {*//*
    override fun renderDropdownEntry(graphics: DrawContext, id: Identifier, n: Int) {
        super.renderDropdownEntry(graphics, id, n)
        graphics.drawItemWithoutEntity(
            ItemStack(matching[id]),
            dimension.xLimit() - decorationPadding - 2,
            dimension.y() + n * dimension.height() + 4
        )
    }
    *//*?} else {*/
    override fun renderDropdownEntry(graphics: DrawContext, entryDimension: Dimension<Int>, id: Identifier) {
        super.renderDropdownEntry(graphics, entryDimension, id)
        graphics.drawItemWithoutEntity(
            ItemStack(matching[id]),
            entryDimension.xLimit() - 2,
            entryDimension.y() + 1)
    }
    /*?} */

    override fun getString(id: Identifier) = "$id"

    override fun getDecorationPadding() = 16

    override fun getDropdownEntryPadding() = 4

    override fun getControlWidth() = super.getControlWidth() + decorationPadding

    override fun getValueText(): Text = when {
        inputField.isEmpty() -> super.getValueText()
        inputFieldFocused -> Text.literal(inputField)
        control.option().pendingValue() is ItemConvertible -> (control.option()
            .pendingValue() as ItemConvertible).asItem().name

        else -> inputField.literal()
    }
}
