package dev.rvbsm.fsit.networking

import net.minecraft.network.PacketByteBuf

internal inline fun <reified T : Enum<T>> PacketByteBuf.readEnumConstant(): T = readEnumConstant(T::class.java)
