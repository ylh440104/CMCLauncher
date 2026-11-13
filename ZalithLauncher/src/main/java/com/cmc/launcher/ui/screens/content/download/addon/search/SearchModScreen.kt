/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

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