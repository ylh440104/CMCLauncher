package com.cmc.launcher.ui.particle

open class Vector2D(var x: Float = 0f, var y: Float = 0f)

fun Vector2D.add(other: Vector2D): Vector2D {
    this.x += other.x
    this.y += other.y
    return this
}

fun Vector2D.add(other: Vector2D, scalar: Float): Vector2D {
    this.x += other.x * scalar
    this.y += other.y * scalar
    return this
}

fun Vector2D.scalarMultiply(scalar: Float): Vector2D {
    this.x *= scalar
    this.y *= scalar
    return this
}

fun Vector2D.inc(factor: Float): Vector2D {
    this.x += factor
    this.y += factor
    return this
}

const val PI = Math.PI
const val HALF_PI = PI / 2
const val TWO_PI = PI * 2
const val QUARTER_PI = PI / 4

fun Float.roundTo(n: Int): Float {
    return this.toBigDecimal().setScale(n, java.math.RoundingMode.UP).toFloat()
}