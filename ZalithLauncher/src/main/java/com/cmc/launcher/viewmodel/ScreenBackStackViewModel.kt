package com.cmc.launcher.viewmodel

import androidx.lifecycle.ViewModel
import com.cmc.launcher.ui.screens.NestedNavKey
import com.cmc.launcher.ui.screens.NormalNavKey

class ScreenBackStackViewModel : ViewModel() {
    /** 主屏幕 */
    val mainScreen = NestedNavKey.Main()
    /** 设置屏幕 */
    val settingsScreen = NestedNavKey.Settings()
    /** 下载屏幕 */
    val downloadScreen = NestedNavKey.Download()

    /** 下载游戏屏幕 */
    val downloadGameScreen = NestedNavKey.DownloadGame()
    /** 下载整合包屏幕 */
    val downloadModPackScreen = NestedNavKey.DownloadModPack()
    /** 下载模组屏幕 */
    val downloadModScreen = NestedNavKey.DownloadMod()
    /** 下载资源包屏幕 */
    val downloadResourcePackScreen = NestedNavKey.DownloadResourcePack()
    /** 下载存档屏幕 */
    val downloadSavesScreen = NestedNavKey.DownloadSaves()
    /** 下载光影包屏幕 */
    val downloadShadersScreen = NestedNavKey.DownloadShaders()
    /** 下载收藏屏幕 */
    val downloadFavoritesScreen = NestedNavKey.DownloadFavorites()

    /**
     * 在跳转前，先将导航栈中所有属于 [clearBeforeNavKeys] 的页面全部移除
     * 这样可以避免用户在这几个页面间产生叠加栈或多层返回的情况
     */
    val clearBeforeNavKeys = listOf(
        settingsScreen::class,
        downloadScreen::class,
        NormalNavKey.Multiplayer::class
    )
}