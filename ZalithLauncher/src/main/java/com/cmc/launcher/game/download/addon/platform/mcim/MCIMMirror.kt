package com.cmc.launcher.game.download.addon.platform.mcim

import com.cmc.launcher.game.addons.mirror.MirrorPriority
import com.cmc.launcher.game.addons.mirror.orderCandidates
import com.cmc.launcher.game.addons.mirror.resolveMirrorPriority
import com.cmc.launcher.setting.AllSettings
import com.cmc.launcher.utils.isChinaMainland

private const val ROOT = "https://mod.mcimirror.top"

private val REPLACE_MIRROR_HOLDERS = listOf(
    //CurseForge
    "https://edge.forgecdn.net",
    //Modrinth
    "https://cdn.modrinth.com"
)

private fun assetPlatformPriority(): MirrorPriority =
    resolveMirrorPriority(AllSettings.addonPlatformSource.getValue(), mainland = true)

/**
 * 按是否中国大陆决定是否启用 MCIM 镜像，并按偏好生成有序候选链接
 */
fun String.mapMCIMMirrorUrls(): List<String> {
    if (!isChinaMainland()) return listOf(this)

    val mirroredUrl = REPLACE_MIRROR_HOLDERS.find { key ->
        startsWith(key)
    }?.let { origin ->
        replaceFirst(origin, ROOT)
    }

    return orderCandidates(official = this, mirror = mirroredUrl, priority = assetPlatformPriority())
}

/**
 * 多链接形式：把数组内可被镜像替换的链接生成镜像版本，按偏好穿插到原列表前/后
 */
fun Array<String>.mapMCIMMirrorUrls(): List<String> {
    if (!isChinaMainland()) return toList()

    val sources = mapNotNull { url ->
        REPLACE_MIRROR_HOLDERS.find { key ->
            url.startsWith(key)
        }?.let { origin ->
            url.replaceFirst(origin, ROOT)
        }
    }
    if (sources.isEmpty()) return toList()

    return when (assetPlatformPriority()) {
        MirrorPriority.OFFICIAL -> this.toList()
        MirrorPriority.MIRROR_FIRST -> sources + this.toList()
    }
}
