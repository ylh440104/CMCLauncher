package com.cmc.launcher.game.version.mod.meta

import com.google.gson.annotations.SerializedName

data class ForgeNewModMetadata(
    @SerializedName("modLoader")
    val modLoader: String? = null,
    @SerializedName("loaderVersion")
    val loaderVersion: String? = null,
    @SerializedName("logoFile")
    val logoFile: String? = null,
    @SerializedName("license")
    val license: String? = null,
    @SerializedName("mods")
    val mods: List<Mod> = emptyList()
) {
    data class Mod(
        @SerializedName("modId")
        val modId: String,
        @SerializedName("version")
        val version: String,
        @SerializedName("displayName")
        val displayName: String,
        @SerializedName("side")
        val side: String? = null,
        @SerializedName("displayURL")
        val displayURL: String? = null,
        @SerializedName("authors")
        val authors: List<String>? = null,
        @SerializedName("description")
        val description: String? = null
    )
}