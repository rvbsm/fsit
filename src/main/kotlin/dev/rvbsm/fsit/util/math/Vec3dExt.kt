package dev.rvbsm.fsit.util.math

import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i


operator fun Vec3d.plus(vec: Vec3d): Vec3d = add(vec)
operator fun Vec3d.plus(vec: Vec3i): Vec3d = Vec3d.add(vec, x, y, z)
operator fun Vec3d.times(value: Double): Vec3d = multiply(value)

fun Vec3d.centered() = Vec3d(x.toInt() + 0.5, y, z.toInt() + 0.5)
