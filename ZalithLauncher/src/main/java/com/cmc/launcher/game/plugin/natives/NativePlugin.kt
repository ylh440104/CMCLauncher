package com.cmc.launcher.game.plugin.natives

import com.cmc.launcher.game.plugin.ApkPlugin
import com.cmc.launcher.game.plugin.Plugin

class NativePlugin(
    packageName: String,
    appName: String,
    appVersion: String,
    val displayName: String,
    val minMCVer: String? = null,
    val maxMCVer: String? = null,
    val path: String,
    val envList: List<String>
): ApkPlugin(
    packageName = packageName,
    appName = appName,
    appVersion = appVersion
), Plugin {
    override fun getIdentifier(): String = packageName
    override fun getNativeLibPath(): String = path
}
