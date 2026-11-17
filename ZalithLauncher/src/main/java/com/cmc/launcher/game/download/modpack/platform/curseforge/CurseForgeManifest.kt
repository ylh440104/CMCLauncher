package com.cmc.launcher.game.download.modpack.platform.curseforge

import com.google.gson.annotations.SerializedName
import com.cmc.launcher.game.download.addon.platform.curseforge.models.curseForgeCDNUrl
import com.cmc.launcher.game.download.modpack.platform.PackManifest

class CurseForgeManifest(
    @SerializedName("manifestType")
    val manifestType: String,
    @SerializedName("manifestVersion")
    val manifestVersion: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("version")
    val version: String,
    @SerializedName("author")
    val author: String,
    @SerializedName("overrides")
    val overrides: String? = null,
    @SerializedName("minecraft")
    val minecraft: Minecraft,
    @SerializedName("files")
    val files: List<ManifestFile>
): PackManifest {
    data class Minecraft(
        @SerializedName("version")
        val gameVersion: String,
        @SerializedName("modLoaders")
        val modLoaders: List<ModLoader>,
        @SerializedName("recommendedRam")
        val recommendedRam: Int? = null
    ) {
        data class ModLoader(
            @SerializedName("id")
            val id: String,
            @SerializedName("primary")
            val primary: Boolean
        )
    }

    data class ManifestFile(
        @SerializedName("projectID")
        val projectID: Int,
        @SerializedName("fileID")
        val fileID: Int,
        @SerializedName("fileName")
        val fileName: String? = null,
        @SerializedName("url")
        val url: String? = null,
        @SerializedName("required")
        val required: Boolean
    ) {
        fun getFileUrl(): String? {
            return url ?: fileName?.let { curseForgeCDNUrl(fileID, it) }
        }
    }
}