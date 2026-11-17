package com.cmc.launcher.setting.enums

/**
 * 背景内容模糊效果类型
 */
enum class BackgroundBlur {
    /**
     * 背景模糊
     */
    Background,
    /**
     * 前景毛玻璃模糊
     */
    Foreground;

    fun switch(): BackgroundBlur {
        return when (this) {
            Background -> Foreground
            Foreground -> Background
        }
    }
}