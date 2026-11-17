package com.cmc.launcher.game.addons.modloader.fabriclike.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FabricLikeVersionsJson(
    @SerialName("game")
    val game: List<FabricLikeGame>,
    @SerialName("loader")
    val loader: List<FabricLikeLoader>
)
