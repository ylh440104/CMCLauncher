package com.cmc.launcher.setting.unit

import android.os.Parcelable
import com.cmc.launcher.setting.launcherMMKV

/**
 * Parcelable 设置单元，将 Parcelable 保存到设置配置文件中
 */
class ParcelableSettingUnit<E: Parcelable>(
    key: String,
    defaultValue: E,
    private val clazz: Class<E>
): AbstractSettingUnit<E>(key, defaultValue) {
    override fun getValue(): E {
        val value: E? = launcherMMKV().decodeParcelable(key, clazz)
        return (value ?: defaultValue)
            .also { state = it }
    }

    override fun saveValue(v: E): E {
        launcherMMKV().encode(key, v)
        return v
    }
}

inline fun <reified E: Parcelable> parcelableSettingUnit(
    key: String,
    defaultValue: E
): ParcelableSettingUnit<E> {
    return ParcelableSettingUnit(
        key = key,
        defaultValue = defaultValue,
        clazz = E::class.java
    )
}