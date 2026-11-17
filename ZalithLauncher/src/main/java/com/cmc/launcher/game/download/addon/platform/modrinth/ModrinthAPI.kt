package com.cmc.launcher.game.download.addon.platform.modrinth

import com.cmc.launcher.game.download.addon.platform.PlatformClasses
import com.cmc.launcher.game.download.addon.platform.PlatformSearchFilter
import com.cmc.launcher.game.download.addon.platform.modrinth.models.ModrinthFacet
import com.cmc.launcher.game.download.addon.platform.modrinth.models.ModrinthModLoaderCategory
import com.cmc.launcher.game.download.addon.platform.modrinth.models.VersionFacet
import com.cmc.launcher.utils.string.isNotEmptyOrBlank

/**
 * Modrinth 平台的 API 链接
 * [Modrinth Docs](https://docs.modrinth.com/api/operations/searchprojects)
 */
const val MODRINTH_API = "https://api.modrinth.com/v2"

/**
 * MCIM 镜像：Modrinth 平台的 API 链接
 * [MCIM Modrinth API](https://github.com/mcmod-info-mirror/mcim-rust-api?tab=readme-ov-file#modrinth)
 */
const val MCIM_MODRINTH_API = "https://mod.mcimirror.top/modrinth/v2"

fun PlatformSearchFilter.toModrinthRequest(
    query: String,
    platformClasses: PlatformClasses
): ModrinthSearchRequest {
    val modrinthVersion = gameVersion.takeIf { it.isNotEmptyOrBlank() }?.let { version ->
        VersionFacet(version.trim())
    }
    val modrinthCategories = categories.map { category ->
        category as? ModrinthFacet
    }.toTypedArray()
    val modrinthModLoader = modloader?.let { modloader ->
        modloader as? ModrinthModLoaderCategory
    }

    return ModrinthSearchRequest(
        query = query,
        facets = listOfNotNull(
            platformClasses.modrinth!!, //必须为非空处理
            modrinthVersion,
            *modrinthCategories,
            modrinthModLoader
        ),
        index = sortField,
        offset = index,
        limit = limit
    )
}