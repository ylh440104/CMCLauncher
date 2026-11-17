package com.cmc.launcher.setting.unit

import com.cmc.launcher.setting.launcherMMKV

class BooleanSettingUnit(key: String, defaultValue: Boolean) : AbstractSettingUnit<Boolean>(key, defaultValue) {
    override fun getValue(): Boolean {
        return launcherMMKV().getBoolean(key, defaultValue)
            .also { state = it }
    }

    override fun saveValue(v: Boolean): Boolean {
        launcherMMKV().putBoolean(key, v).apply()
        return v
    }
}