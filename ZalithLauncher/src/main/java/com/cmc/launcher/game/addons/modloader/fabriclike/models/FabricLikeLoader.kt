package com.cmc.launcher.game.addons.modloader.fabriclike.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FabricLikeLoader(
    @SerialName("separator")
    val separator: String,
    @SerialName("build")
    val build: Int,
    @SerialName("maven")
    val maven: String,
    /** 加载器版本 */
    @SerialName("version")
    val version: String,
    /** 版本状态: true 为稳定版 (Quilt忽略此值) */
    @SerialName("stable")
    val stable: Boolean = true
)