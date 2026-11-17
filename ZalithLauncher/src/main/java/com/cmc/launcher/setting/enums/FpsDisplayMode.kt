package com.cmc.launcher.setting.enums

import com.cmc.launcher.R

/**
 * 游戏内帧率的展示模式
 */
enum class FpsDisplayMode(val nameRes: Int) {
    NUMBER(R.string.game_menu_option_fps_display_mode_number),
    CHART(R.string.game_menu_option_fps_display_mode_chart)
}
