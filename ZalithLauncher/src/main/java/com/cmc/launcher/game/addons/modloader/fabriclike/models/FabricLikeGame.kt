package com.cmc.launcher.game.addons.modloader.fabriclike.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FabricLikeGame(
    /** 对应的 Minecraft 版本 */
    @SerialName("version")
    val version: String,
    /** 是否为正式版 */
    @SerialName("stable")
    val stable: Boolean
)
