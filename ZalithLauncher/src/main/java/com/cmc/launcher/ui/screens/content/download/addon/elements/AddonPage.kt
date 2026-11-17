package com.cmc.launcher.ui.screens.content.download.addon.elements

import com.cmc.launcher.game.download.addon.platform.PlatformSearchData
import org.jackhuang.hmcl.addon.AddonProjectInfo

/**
 * 资源搜索结果页面信息
 * @param pageNumber 第几页
 * @param pageIndex 页面索引
 * @param totalPage 总页数
 * @param isLastPage 是否为最后一页
 * @param data 搜索结果缓存
 */
data class AddonPage(
    val pageNumber: Int,
    val pageIndex: Int,
    val totalPage: Int,
    val isLastPage: Boolean,
    val data: List<Pair<PlatformSearchData, AddonProjectInfo?>>
)