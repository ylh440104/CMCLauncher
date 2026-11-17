package com.cmc.launcher.game.download.addon.platform

data class PlatformSearchFilter(
    val searchName: String = "",
    val gameVersion: String = "",
    val sortField: PlatformSortField = PlatformSortField.RELEVANCE,
    val categories: List<PlatformFilterCode> = emptyList(),
    val modloader: PlatformDisplayLabel? = null,
    val index: Int = 0,
    val limit: Int = 20
)