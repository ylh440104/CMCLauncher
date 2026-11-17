package com.cmc.launcher.setting.enums

import com.movtery.inputmap.keycodes.LwjglGlfwKeycode
import com.cmc.launcher.R

/**
 * 手势控制点击时触发的按钮
 */
enum class GestureActionType(val nameRes: Int) {
    MOUSE_RIGHT(R.string.settings_control_gesture_trigger_mouse_right),
    MOUSE_LEFT(R.string.settings_control_gesture_trigger_mouse_left)
}

/**
 * 转换为实际的Lwjgl键值
 */
fun GestureActionType.toAction(): Int =
    when (this) {
        GestureActionType.MOUSE_RIGHT -> LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_RIGHT
        GestureActionType.MOUSE_LEFT -> LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LEFT
    }.toInt()