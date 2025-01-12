package dev.rvbsm.fsit.util.math

import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import kotlin.math.floor


operator fun Vec3d.plus(vec: Vec3d): Vec3d = add(vec)
operator fun Vec3d.plus(vec: Vec3i): Vec3d = Vec3d.add(vec, x, y, z)
operator fun Vec3d.times(value: Double): Vec3d = multiply(value)

fun Vec3d.centered() = Vec3d(floor(x) + 0.5, y, floor(z) + 0.5)
