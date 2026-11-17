package com.cmc.launcher.setting.unit

val IntSettingUnit.min: Int
    get() = valueRange.first

val IntSettingUnit.max: Int
    get() = valueRange.last

val NullableIntSettingUnit.min: Int
    get() = valueRange.first

val NullableIntSettingUnit.max: Int
    get() = valueRange.last

fun NullableIntSettingUnit.getOrMin(): Int = getValue() ?: min

fun NullableIntSettingUnit.getOrMax(): Int = getValue() ?: max

val FloatSettingUnit.min: Float
    get() = valueRange.start

val FloatSettingUnit.max: Float
    get() = valueRange.endInclusive

val LongSettingUnit.min: Long
    get() = valueRange.first

val LongSettingUnit.max: Long
    get() = valueRange.last