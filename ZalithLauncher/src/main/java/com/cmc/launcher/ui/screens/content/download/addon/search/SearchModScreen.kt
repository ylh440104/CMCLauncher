package com.cmc.launcher.ui.screens.content.download.addon.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.cmc.launcher.game.download.addon.platform.Platform
import com.cmc.launcher.game.download.addon.platform.PlatformClasses
import com.cmc.launcher.game.download.addon.platform.curseforge.models.CurseForgeModCategory
import com.cmc.launcher.game.download.addon.platform.curseforge.models.curseForgeModLoaderFilters
import com.cmc.launcher.game.download.addon.platform.modrinth.models.ModrinthFeatures
import com.cmc.launcher.game.download.addon.platform.modrinth.models.ModrinthModCategory
import com.cmc.launcher.game.download.addon.platform.modrinth.models.modrinthModLoaderFilters
import com.cmc.launcher.game.version.mod.InstalledMod
import com.cmc.launcher.setting.AllSettings
import com.cmc.launcher.ui.screens.NormalNavKey
import com.cmc.launcher.ui.screens.TitledNavKey

@Composable
fun SearchModScreen(
    mainScreenKey: TitledNavKey?,
    downloadScreenKey: TitledNavKey?,
    downloadModScreenKey: TitledNavKey,
    downloadModScreenCurrentKey: TitledNavKey?,
    onPlatformChange: (Platform) -> Unit = {},
    installedInfo: ((Platform, projectId: String) -> InstalledMod?)? = null,
    swapToDownload: (Platform, projectId: String, iconUrl: String?) -> Unit = { _, _, _ -> }
) {
    val initialPlatform = remember {
        AllSettings.searchModPlatform.getValue()
    }
    SearchAddonScreen(
        mainScreenKey = mainScreenKey,
        parentScreenKey = downloadModScreenKey,
        parentCurrentKey = downloadScreenKey,
        screenKey = NormalNavKey.SearchMod,
        currentKey = downloadModScreenCurrentKey,
        platformClasses = PlatformClasses.MOD,
        initialPlatform = initialPlatform,
        onPlatformChange = {
            AllSettings.searchModPlatform.save(it)
            onPlatformChange(it)
        },
        getCategories = { platform ->
            when (platform) {
                Platform.CURSEFORGE -> CurseForgeModCategory.entries
                Platform.MODRINTH -> ModrinthModCategory.entries
            }
        },
        enableModLoader = true,
        getModloaders = { platform ->
            when (platform) {
                Platform.CURSEFORGE -> curseForgeModLoaderFilters
                Platform.MODRINTH -> modrinthModLoaderFilters
            }
        },
        mapCategories = { platform, string ->
            when (platform) {
                Platform.MODRINTH -> {
                    ModrinthModCategory.entries.find { it.facetValue() == string }
                        ?: ModrinthFeatures.entries.find { it.facetValue() == string }
                }
                Platform.CURSEFORGE -> {
                    CurseForgeModCategory.entries.find { it.describe() == string }
                }
            }
        },
        swapToDownload = swapToDownload,
        installedInfo = installedInfo
    )
}