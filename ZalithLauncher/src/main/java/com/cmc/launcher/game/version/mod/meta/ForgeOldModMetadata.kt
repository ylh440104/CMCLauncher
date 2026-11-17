package com.cmc.launcher.game.version.mod.meta

import com.google.gson.annotations.SerializedName

data class ForgeOldModMetadata(
    @SerializedName("modid")
    val modId: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("description")
    val description: String = "",
    @SerializedName("author")
    val author: String = "",
    @SerializedName("version")
    val version: String = "",
    @SerializedName("logoFile")
    val logoFile: String = "",
    @SerializedName("mcversion")
    val gameVersion: String = "",
    @SerializedName("url")
    val url: String = "",
    @SerializedName("updateUrl")
    val updateUrl: String = "",
    @SerializedName("credits")
    val credits: String = "",
    @SerializedName("authorList")
    val authorList: List<String> = emptyList(),
    @SerializedName("authors")
    val authors: List<String> = emptyList()
)