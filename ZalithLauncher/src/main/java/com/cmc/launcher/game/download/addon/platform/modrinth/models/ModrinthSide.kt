package com.cmc.launcher.game.download.addon.platform.modrinth.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ModrinthSide {
    @SerialName("required")
    REQUIRED,

    @SerialName("optional")
    OPTIONAL,

    @SerialName("unsupported")
    UNSUPPORTED,

    @SerialName("unknown")
    UNKNOWN
}