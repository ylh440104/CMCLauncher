package com.cmc.launcher.setting.enums

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.cmc.launcher.R
import com.cmc.launcher.setting.AllSettings

/**
 * 启动器UI深色主题
 */
enum class DarkMode(val textRes: Int) {
    Enable(R.string.generic_enable),
    Disable(R.string.generic_disable),

    /**
     * 跟随系统变化
     */
    FollowSystem(R.string.generic_follow_system)
}

/**
 * 当前启动器是否处于深色主题模式中，和 [isSystemInDarkTheme] 函数使用方法一致，
 * 但引入了启动器的设置系统的干预
 */
@Composable
fun isLauncherInDarkTheme(): Boolean {
    val value = AllSettings.launcherDarkMode.state
    return key(value) {
        when (value) {
            DarkMode.Enable -> true
            DarkMode.Disable -> false
            DarkMode.FollowSystem -> isSystemInDarkTheme()
        }
    }
}