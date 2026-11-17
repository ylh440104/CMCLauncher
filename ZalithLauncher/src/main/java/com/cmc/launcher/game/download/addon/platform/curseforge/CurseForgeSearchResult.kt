package com.cmc.launcher.game.download.addon.platform.curseforge

import com.cmc.launcher.game.download.addon.platform.PlatformClasses
import com.cmc.launcher.game.download.addon.platform.PlatformSearchResult
import com.cmc.launcher.game.download.addon.platform.curseforge.models.CurseForgeData
import com.cmc.launcher.game.download.addon.platform.curseforge.models.CurseForgePagination
import com.cmc.launcher.game.download.addon.platform.curseforge.models.isApproved
import com.cmc.launcher.game.download.addon.platform.searchRankWithChineseBias
import com.cmc.launcher.game.download.addon.utils.getTranslations
import com.cmc.launcher.ui.screens.content.download.addon.elements.AddonPage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CurseForgeSearchResult(
    /**
     * 响应数据
     */
    @SerialName("data")
    private val data: Array<CurseForgeData>,

    /**
     * 响应分页信息
     */
    @SerialName("pagination")
    private val pagination: CurseForgePagination
): PlatformSearchResult {
    override fun getAddonPage(classes: PlatformClasses): AddonPage {
        val mcmodData = data.mapNotNull { data0 ->
            if (!data0.isApproved()) return@mapNotNull null
            data0 to classes.getTranslations()?.getInfoByCurseForgeId(data0.slug)
        }
        val pageSize = pagination.pageSize
        val isLastPage = pagination.resultCount < pageSize ||
                (pagination.index + pagination.resultCount) >= pagination.totalCount

        return AddonPage(
            pageNumber = pagination.index / pageSize + 1,
            pageIndex = pagination.index,
            totalPage = ((pagination.totalCount + pageSize - 1) / pageSize).toInt(),
            isLastPage = isLastPage,
            data = mcmodData
        )
    }

    override fun processChineseSearchResults(
        searchFilter: String,
        classes: PlatformClasses
    ): PlatformSearchResult {
        val newData = data.filter { it.isApproved() }
            .searchRankWithChineseBias(searchFilter, classes) { it.slug }
            .toTypedArray()
        return CurseForgeSearchResult(newData, pagination)
    }
}