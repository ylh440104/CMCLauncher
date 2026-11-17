package com.cmc.launcher.game.launch

import com.cmc.launcher.game.version.installed.utils.isLowerOrEqualVer
import java.util.Locale

private fun getLanguage(versionId: String): String {
    val locate = Locale.getDefault()
    return if (versionId.isLowerOrEqualVer("1.10.2")) {
        locate.language + "_" + locate.country.uppercase() // 1.10 -
    } else {
        locate.language + "_" + locate.country.lowercase()
    }
}

fun MCOptions.loadLanguage(versionId: String) {
    if (!containsKey("lang")) {
        val lang = getLanguage(versionId)
        set("lang", lang)
    }
}