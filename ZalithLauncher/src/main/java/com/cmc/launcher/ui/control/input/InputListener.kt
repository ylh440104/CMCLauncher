package com.cmc.launcher.ui.control.input

interface InputListener {
    fun onSend(char: Char)
    fun onBackspace()
    fun onEnter()
}