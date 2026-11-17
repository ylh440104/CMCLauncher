package com.cmc.launcher.game.plugin

interface Plugin {
    fun getIdentifier(): String
    fun getNativeLibPath(): String
}