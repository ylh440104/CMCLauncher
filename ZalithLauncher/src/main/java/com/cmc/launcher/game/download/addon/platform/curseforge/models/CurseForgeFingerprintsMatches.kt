package com.cmc.launcher.game.download.addon.platform.curseforge.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CurseForgeFingerprintsMatches(
    @SerialName("data")
    val data: Result
) {
    @Serializable
    data class Result(
        @SerialName("isCacheBuilt")
        val isCacheBuilt: Boolean,
        @SerialName("exactMatches")
        val exactMatches: List<FingerprintMatch>? = null,
        @SerialName("exactFingerprints")
        val exactFingerprints: List<Long>? = null,
        @SerialName("partialMatches")
        val partialMatches: List<FingerprintMatch>? = null,
        @SerialName("partialMatchFingerprints")
        val partialMatchFingerprints: JsonElement? = null,
        @SerialName("additionalProperties")
        val additionalProperties: List<Long>? = null,
        @SerialName("installedFingerprints")
        val installedFingerprints: List<Long>? = null,
        @SerialName("unmatchedFingerprints")
        val unmatchedFingerprints: List<Long>? = null
    ) {
        @Serializable
        data class FingerprintMatch(
            @SerialName("id")
            val id: Int,
            @SerialName("file")
            val file: CurseForgeFile,
            @SerialName("latestFiles")
            val latestFiles: List<CurseForgeFile>
        )
    }
}