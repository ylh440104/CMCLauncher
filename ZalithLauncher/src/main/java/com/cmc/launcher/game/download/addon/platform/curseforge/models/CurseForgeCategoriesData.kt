package com.cmc.launcher.game.download.addon.platform.curseforge.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurseForgeCategoriesData(
    @SerialName("data")
    val data: List<Data>
) {
    @Serializable
    data class Data(
        @SerialName("classId")
        val classId: Int? = null,
        @SerialName("dateModified")
        val dateModified: String,
        @SerialName("displayIndex")
        val displayIndex: Int,
        @SerialName("gameId")
        val gameId: Int,
        @SerialName("iconUrl")
        val iconUrl: String,
        @SerialName("id")
        val id: Int,
        @SerialName("isClass")
        val isClass: Boolean? = null,
        @SerialName("name")
        val name: String,
        @SerialName("parentCategoryId")
        val parentCategoryId: Int? = null,
        @SerialName("slug")
        val slug: String,
        @SerialName("url")
        val url: String
    )
}