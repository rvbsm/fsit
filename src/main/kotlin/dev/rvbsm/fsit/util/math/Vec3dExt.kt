package dev.rvbsm.fsit.util.math

import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

fun Vec3d.clamp(absMin: Double) = Vec3d(
    x.takeIf { abs(it) >= absMin } ?: 0.0,
    y.takeIf { abs(it) >= absMin } ?: 0.0,
    z.takeIf { abs(it) >= absMin } ?: 0.0,
)

fun Vec3d.addHorizontal(speed: Float, yaw: Float): Vec3d = add(
    speed * sin(-yaw / 180.0 * PI),
    0.0,
    speed * cos(yaw / 180.0 * PI),
)

operator fun Vec3d.plus(vec: Vec3d): Vec3d = add(vec)
operator fun Vec3d.plus(vec: Vec3i): Vec3d = Vec3d.add(vec, x, y, z)
operator fun Vec3d.times(value: Double): Vec3d = multiply(value)

fun Vec3d.centered() = Vec3d(x.toInt() + 0.5, y, z.toInt() + 0.5)
