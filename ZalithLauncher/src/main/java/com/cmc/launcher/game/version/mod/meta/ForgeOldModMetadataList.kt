package com.cmc.launcher.game.version.mod.meta

import com.google.gson.annotations.SerializedName

// from https://github.com/HMCL-dev/HMCL/commit/2c428faa9540e8666ce3cec07ff284cb53eab34a
class ForgeOldModMetadataList(
    @SerializedName("modListVersion")
    val modListVersion: Int?,
    @SerializedName("modList")
    val modList: List<ForgeOldModMetadata>?
)