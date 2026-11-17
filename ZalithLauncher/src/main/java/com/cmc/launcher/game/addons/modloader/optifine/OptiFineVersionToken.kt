package com.cmc.launcher.game.addons.modloader.optifine

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OptiFineVersionToken(
    @SerialName("mcversion")
    val mcVersion: String,
    @SerialName("type")
    val type: String,
    @SerialName("patch")
    val patch: String,
    @SerialName("filename")
    val fileName: String,
    @SerialName("forge")
    val forge: String? = null
)