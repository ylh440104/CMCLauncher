package com.cmc.launcher.game.download.addon.platform

import com.cmc.launcher.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PlatformSortField(
    val curseforge: String,
    val modrinth: String
): PlatformFilterCode {
    /** 相关 */
    @SerialName("RELEVANCE")
    RELEVANCE("1", "relevance") {
        override fun getDisplayName(): Int = R.string.download_addon_filter_sort_by_relevant
    },

    /** 下载量 */
    @SerialName("DOWNLOADS")
    DOWNLOADS("6", "downloads") {
        override fun getDisplayName(): Int = R.string.download_addon_filter_sort_by_total_downloads
    },

    /** 人气 */
    @SerialName("POPULARITY")
    POPULARITY("2", "follows") {
        override fun getDisplayName(): Int = R.string.download_addon_filter_sort_by_popularity
    },

    /** 新创建 */
    @SerialName("NEWEST")
    NEWEST("11", "newest") {
        override fun getDisplayName(): Int = R.string.download_addon_filter_sort_by_recently_created
    },

    /** 最近更新 */
    @SerialName("UPDATED")
    UPDATED("3", "updated") {
        override fun getDisplayName(): Int = R.string.download_addon_filter_sort_by_recently_updated
    };

    override fun index(): Int = this.ordinal
}