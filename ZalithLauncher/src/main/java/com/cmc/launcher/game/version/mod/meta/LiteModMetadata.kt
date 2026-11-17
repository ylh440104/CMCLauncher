package com.cmc.launcher.game.version.mod.meta

import com.google.gson.annotations.SerializedName

data class LiteModMetadata(
    @SerializedName("name")
    val name: String,
    @SerializedName("version")
    val version: String,
    @SerializedName("mcversion")
    val mcversion: String,
    @SerializedName("revision")
    val revision: String? = null,
    @SerializedName("author")
    val author: String? = null,
    @SerializedName("classTransformerClasses")
    val classTransformerClasses: Array<String>? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("modpackName")
    val modpackName: String? = null,
    @SerializedName("modpackVersion")
    val modpackVersion: String? = null,
    @SerializedName("checkUpdateUrl")
    val checkUpdateUrl: String? = null,
    @SerializedName("updateURI")
    val updateURI: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as LiteModMetadata
        return classTransformerClasses.contentEquals(other.classTransformerClasses)
    }

    override fun hashCode(): Int {
        return classTransformerClasses?.contentHashCode() ?: 0
    }
}