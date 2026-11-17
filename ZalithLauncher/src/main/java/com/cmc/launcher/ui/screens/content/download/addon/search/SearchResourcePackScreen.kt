package com.cmc.launcher.ui.screens.content.download.addon.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.cmc.launcher.game.download.addon.platform.Platform
import com.cmc.launcher.game.download.addon.platform.PlatformClasses
import com.cmc.launcher.game.download.addon.platform.curseforge.models.CurseForgeResourcePackCategory
import com.cmc.launcher.game.download.addon.platform.modrinth.models.ModrinthFeatures
import com.cmc.launcher.game.download.addon.platform.modrinth.models.ModrinthResourcePackCategory
import com.cmc.launcher.setting.AllSettings
import com.cmc.launcher.ui.screens.NormalNavKey
import com.cmc.launcher.ui.screens.TitledNavKey

@Composable
fun SearchResourcePackScreen(
    mainScreenKey: TitledNavKey?,
    downloadScreenKey: TitledNavKey?,
    downloadResourcePackScreenKey: TitledNavKey,
    downloadResourcePackScreenCurrentKey: TitledNavKey?,
    swapToDownload: (Platform, projectId: String, iconUrl: String?) -> Unit = { _, _, _ -> }
) {
    val initialPlatform = remember {
        AllSettings.searchResourcePackPlatform.getValue()
    }
    SearchAddonScreen(
        mainScreenKey = mainScreenKey,
        parentScreenKey = downloadResourcePackScreenKey,
        parentCurrentKey = downloadScreenKey,
        screenKey = NormalNavKey.SearchResourcePack,
        currentKey = downloadResourcePackScreenCurrentKey,
        platformClasses = PlatformClasses.RESOURCE_PACK,
        initialPlatform = initialPlatform,
        onPlatformChange = {
            AllSettings.searchResourcePackPlatform.save(it)
        },
        getCategories = { platform ->
            when (platform) {
                Platform.CURSEFORGE -> CurseForgeResourcePackCategory.entries
                Platform.MODRINTH -> ModrinthResourcePackCategory.entries
            }
        },
        mapCategories = { platform, string ->
            when (platform) {
                Platform.MODRINTH -> {
                    ModrinthResourcePackCategory.entries.find { it.facetValue() == string }
                        ?: ModrinthFeatures.entries.find { it.facetValue() == string }
                }
                Platform.CURSEFORGE -> {
                    CurseForgeResourcePackCategory.entries.find { it.describe() == string }
                }
            }
        },
        swapToDownload = swapToDownload
    )
}