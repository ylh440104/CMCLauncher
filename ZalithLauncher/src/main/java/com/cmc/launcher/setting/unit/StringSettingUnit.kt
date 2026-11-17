package com.cmc.launcher.setting.unit

import com.cmc.launcher.setting.launcherMMKV

class StringSettingUnit(key: String, defaultValue: String) : AbstractSettingUnit<String>(key, defaultValue) {
    override fun getValue(): String {
        return launcherMMKV().getString(key ,defaultValue)!!
            .also { state = it }
    }

    override fun saveValue(v: String): String {
        launcherMMKV().putString(key, v).apply()
        return v
    }
}