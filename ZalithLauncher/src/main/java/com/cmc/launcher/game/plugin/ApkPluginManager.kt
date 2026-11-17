package com.cmc.launcher.game.plugin

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Bundle
import com.cmc.launcher.utils.string.isNotEmptyOrBlank

abstract class ApkPluginManager {
    abstract fun parseApkPlugin(
        context: Context,
        info: ApplicationInfo,
        loaded: (ApkPlugin) -> Unit = {}
    )

    protected fun Bundle.getVersionString(key: String): String? {
        return if (containsKey(key)) {
            runCatching {
                when (val o = get(key)) {
                    is String -> o
                    is Number -> o.toString()
                    else -> null
                }
            }.getOrNull()?.takeIf { it.isNotEmptyOrBlank() }
        } else null
    }
}