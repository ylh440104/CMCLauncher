package com.cmc.launcher.setting.unit

import androidx.compose.ui.geometry.Offset
import com.cmc.launcher.setting.launcherMMKV

class OffsetSettingUnit(
    key: String,
    defaultValue: Offset
) : AbstractSettingUnit<Offset>(key, defaultValue) {
    override fun getValue(): Offset {
        val long = launcherMMKV().getLong(key, defaultValue.packedValue)
        return Offset(long).also { state = it }
    }

    override fun saveValue(v: Offset): Offset {
        launcherMMKV().putLong(key, v.packedValue).apply()
        return v
    }
}