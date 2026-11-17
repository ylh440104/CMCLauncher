package com.cmc.launcher.game.addons.modloader.forgelike.neoforge.models

import com.cmc.launcher.game.addons.modloader.forgelike.neoforge.NeoForgeVersion

interface NeoForgeMergeableMaven<E>  {
    /**
     * 将自己的版本数据与其他的版本数据进行合并
     */
    operator fun plus(maven: E): List<NeoForgeVersion>

    fun isVersionInvalid(versionId: String): Boolean {
        val cantDownload = versionId == "47.1.82" //这个版本虽然在版本列表中，但不能下载
        val isAlpha = versionId.contains("-alpha") //Alpha版本不太稳定，避免下载
        return cantDownload || isAlpha
    }
}