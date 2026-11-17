package com.cmc.launcher.setting.enums

import com.cmc.launcher.R

/**
 * 虚拟鼠标的半屏触摸阻止模式
 */
enum class MouseTouchBlockMode(val nameRes: Int) {
    NONE(R.string.generic_close),
    /** 阻止左半屏的触摸输入 */
    LEFT(R.string.settings_control_mouse_block_touch_left),
    /** 阻止右半屏的触摸输入 */
    RIGHT(R.string.settings_control_mouse_block_touch_right)
}
