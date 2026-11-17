package com.cmc.launcher.ui.screens.content.download.addon.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.cmc.launcher.game.download.addon.platform.Platform
import com.cmc.launcher.game.download.addon.platform.PlatformClasses
import com.cmc.launcher.game.download.addon.platform.curseforge.models.CurseForgeShadersCategory
import com.cmc.launcher.game.download.addon.platform.modrinth.models.ModrinthFeatures
import com.cmc.launcher.game.download.addon.platform.modrinth.models.ModrinthShadersCategory
import com.cmc.launcher.setting.AllSettings
import com.cmc.launcher.ui.screens.NormalNavKey
import com.cmc.launcher.ui.screens.TitledNavKey

@Composable
fun SearchShadersScreen(
    mainScreenKey: TitledNavKey?,
    downloadScreenKey: TitledNavKey?,
    downloadShadersScreenKey: TitledNavKey,
    downloadShadersScreenCurrentKey: TitledNavKey?,
    swapToDownload: (Platform, projectId: String, iconUrl: String?) -> Unit = { _, _, _ -> }
) {
    val initialPlatform = remember {
        AllSettings.searchShadersPlatform.getValue()
    }
    SearchAddonScreen(
        mainScreenKey = mainScreenKey,
        parentScreenKey = downloadShadersScreenKey,
        parentCurrentKey = downloadScreenKey,
        screenKey = NormalNavKey.SearchShaders,
        currentKey = downloadShadersScreenCurrentKey,
        platformClasses = PlatformClasses.SHADERS,
        initialPlatform = initialPlatform,
        onPlatformChange = {
            AllSettings.searchShadersPlatform.save(it)
        },
        getCategories = { platform ->
            when (platform) {
                Platform.CURSEFORGE -> CurseForgeShadersCategory.entries
                Platform.MODRINTH -> ModrinthShadersCategory.entries
            }
        },
        mapCategories = { platform, string ->
            when (platform) {
                Platform.MODRINTH -> {
                    ModrinthShadersCategory.entries.find { it.facetValue() == string }
                        ?: ModrinthFeatures.entries.find { it.facetValue() == string }
                }
                Platform.CURSEFORGE -> {
                    CurseForgeShadersCategory.entries.find { it.describe() == string }
                }
            }
        },
        swapToDownload = swapToDownload
    )
}