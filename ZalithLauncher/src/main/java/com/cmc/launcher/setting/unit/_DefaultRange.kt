package com.cmc.launcher.setting.unit

val DEFAULT_INT_RANGE: IntRange = Int.MIN_VALUE..Int.MAX_VALUE

val DEFAULT_FLOAT_RANGE: ClosedFloatingPointRange<Float> = Float.MIN_VALUE..Float.MAX_VALUE

val DEFAULT_LONG_RANGE: LongRange = Long.MIN_VALUE..Long.MAX_VALUE

fun IntRange.toFloatRange(): ClosedFloatingPointRange<Float> = start.toFloat()..endInclusive.toFloat()

val IntSettingUnit.floatRange: ClosedFloatingPointRange<Float>
    get() = valueRange.toFloatRange()

val NullableIntSettingUnit.floatRange: ClosedFloatingPointRange<Float>
    get() = valueRange.toFloatRange()