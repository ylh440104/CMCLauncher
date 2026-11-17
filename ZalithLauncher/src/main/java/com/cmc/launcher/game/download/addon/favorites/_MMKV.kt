package com.cmc.launcher.game.download.addon.favorites

import com.cmc.launcher.game.download.addon.platform.Platform
import com.tencent.mmkv.MMKV

/**
 * Modrinth 收藏项目缓存 MMKV，项目ID对应项目数据
 */
fun favoritesModrinth(): MMKV = MMKV.mmkvWithID("FavoriteModrinthProject")
/**
 * CurseForge 收藏项目缓存 MMKV，项目ID对应项目数据
 */
fun favoritesCurseForge(): MMKV = MMKV.mmkvWithID("FavoriteCurseForgeProject")

/**
 * 按平台获取收藏项目缓存 MMKV
 */
fun favoritesMMKV(platform: Platform): MMKV = when (platform) {
    Platform.MODRINTH -> favoritesModrinth()
    Platform.CURSEFORGE -> favoritesCurseForge()
}
