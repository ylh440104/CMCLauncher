package com.cmc.launcher.game.input

import android.view.KeyEvent
import android.view.MotionEvent
import com.cmc.launcher.bridge.ZLBridge

object AWTCharSender : CharacterSenderStrategy {
    override fun sendChar(character: Char) {
        ZLBridge.sendChar(character)
    }

    override fun sendBackspace() {
        ZLBridge.sendKey(' ', AWTInputEvent.VK_BACK_SPACE)
    }

    override fun sendLeft() {
        ZLBridge.sendKey(' ', AWTInputEvent.VK_LEFT)
    }

    override fun sendRight() {
        ZLBridge.sendKey(' ', AWTInputEvent.VK_RIGHT)
    }

    override fun sendUp() {
        ZLBridge.sendKey(' ', AWTInputEvent.VK_UP)
    }

    override fun sendDown() {
        ZLBridge.sendKey(' ', AWTInputEvent.VK_DOWN)
    }

    override fun sendEnter() {
        ZLBridge.sendKey(' ', AWTInputEvent.VK_ENTER)
    }

    override fun sendTab() {
        ZLBridge.sendKey(' ', AWTInputEvent.VK_TAB)
    }

    override fun sendOther(key: KeyEvent) {
        // Ignore
    }

    override fun sendCopy() {
        sendModifierCtrl(true)
        ZLBridge.sendKey(' ', AWTInputEvent.VK_C)
        sendModifierCtrl(false)
    }

    override fun sendCut() {
        sendModifierCtrl(true)
        ZLBridge.sendKey(' ', AWTInputEvent.VK_X)
        sendModifierCtrl(false)
    }

    override fun sendPaste() {
        sendModifierCtrl(true)
        ZLBridge.sendKey(' ', AWTInputEvent.VK_V)
        sendModifierCtrl(false)
    }

    override fun sendSelectAll() {
        sendModifierCtrl(true)
        ZLBridge.sendKey(' ', AWTInputEvent.VK_A)
        sendModifierCtrl(false)
    }

    override fun sendModifierShift(press: Boolean) {
        ZLBridge.sendKey(' ', AWTInputEvent.VK_SHIFT, if (press) 1 else 0)
    }

    override fun sendModifierCtrl(press: Boolean) {
        ZLBridge.sendKey(' ', AWTInputEvent.VK_CONTROL, if (press) 1 else 0)
    }

    /**
     * 获取 AWT 鼠标点击事件
     */
    fun getMouseButton(button: Int): Int? {
        return when (button) {
            MotionEvent.BUTTON_PRIMARY -> AWTInputEvent.BUTTON1_DOWN_MASK
            MotionEvent.BUTTON_SECONDARY, MotionEvent.BUTTON_STYLUS_SECONDARY -> AWTInputEvent.BUTTON3_DOWN_MASK
            MotionEvent.BUTTON_TERTIARY -> AWTInputEvent.BUTTON2_DOWN_MASK
            else -> null
        }
    }
}