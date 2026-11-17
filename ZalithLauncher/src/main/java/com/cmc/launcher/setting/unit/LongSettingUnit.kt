package com.cmc.launcher.setting.unit

import com.cmc.launcher.setting.launcherMMKV

class LongSettingUnit(
    key: String,
    defaultValue: Long,
    val valueRange: LongRange
) : AbstractSettingUnit<Long>(key, defaultValue) {
    override fun getValue(): Long {
        return launcherMMKV().getLong(key, defaultValue)
            .also { state = it }
    }

    override fun saveValue(v: Long): Long {
        return v.coerceIn(valueRange).also { value ->
            launcherMMKV().putLong(key, value).apply()
        }
    }

    override fun updateState(value: Long) {
        this.state = value.coerceIn(valueRange)
    }
}