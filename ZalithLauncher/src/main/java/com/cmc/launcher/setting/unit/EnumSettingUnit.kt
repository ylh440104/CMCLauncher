package com.cmc.launcher.setting.unit

import com.cmc.launcher.setting.launcherMMKV

/**
 * 枚举设置单元，将枚举保存到设置配置文件中
 */
class EnumSettingUnit<E : Enum<E>>(
    key: String,
    defaultValue: E,
    private val getEnum: (String) -> E?
) : AbstractSettingUnit<E>(key, defaultValue) {
    override fun getValue(): E {
        val valueString: String = launcherMMKV().getString(key, defaultValue.name)!!
        return (getEnum(valueString) ?: defaultValue)
            .also { state = it }
    }

    override fun saveValue(v: E): E {
        launcherMMKV().putString(key, v.name).apply()
        return v
    }
}

inline fun <reified E : Enum<E>> enumSettingUnit(
    key: String,
    defaultValue: E
): EnumSettingUnit<E> {
    return EnumSettingUnit(
        key = key,
        defaultValue = defaultValue,
        getEnum = { raw -> enumValues<E>().firstOrNull { it.name == raw } }
    )
}
