package com.cmc.launcher.setting.unit

import com.cmc.launcher.setting.launcherMMKV

class FloatSettingUnit(
    key: String,
    defaultValue: Float,
    val valueRange: ClosedFloatingPointRange<Float>
) : AbstractSettingUnit<Float>(key, defaultValue) {
    override fun getValue(): Float {
        return launcherMMKV().getFloat(key, defaultValue)
            .also { state = it }
    }

    override fun saveValue(v: Float): Float {
        return v.coerceIn(valueRange).also { value ->
            launcherMMKV().putFloat(key, value).apply()
        }
    }

    override fun updateState(value: Float) {
        this.state = value.coerceIn(valueRange)
    }
}