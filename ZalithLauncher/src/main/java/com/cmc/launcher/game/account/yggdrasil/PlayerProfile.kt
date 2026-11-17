package com.cmc.launcher.game.account.yggdrasil

import com.cmc.launcher.game.account.wardrobe.SkinModelType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.io.File

@Serializable
data class PlayerProfile(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("skins")
    val skins: List<Skin>,
    @SerialName("capes")
    val capes: List<Cape>,
    @SerialName("profileActions")
    val profileActions: JsonElement? = null
) {
    @Serializable
    data class Skin(
        @SerialName("id")
        val id: String,
        @SerialName("state")
        val state: String,
        @SerialName("url")
        val url: String,
        @SerialName("textureKey")
        val textureKey: String,
        @SerialName("variant")
        val variant: String
    )

    @Serializable
    data class Cape(
        @SerialName("id")
        val id: String,
        @SerialName("state")
        val state: String,
        @SerialName("url")
        val url: String,
        @SerialName("alias")
        val alias: String
    )
}

/**
 * 该皮肤是否正在使用中
 */
fun PlayerProfile.Skin.isUsing(): Boolean = this.state == "ACTIVE"

/**
 * 该披风是否正在使用中
 */
fun PlayerProfile.Cape.isUsing(): Boolean = this.state == "ACTIVE"

/**
 * 查找玩家当前正在使用的皮肤
 */
fun List<PlayerProfile.Skin>.findUsing(): PlayerProfile.Skin? = this.find { it.isUsing() }

/**
 * 查找玩家当前正在使用的披风
 */
fun List<PlayerProfile.Cape>.findUsing(): PlayerProfile.Cape? = this.find { it.isUsing() }

/**
 * 获取玩家皮肤模型类型
 */
fun PlayerProfile.Skin.getSkinModel(): SkinModelType {
    return when (variant) {
        "CLASSIC" -> SkinModelType.STEVE
        "SLIM" -> SkinModelType.ALEX
        else -> SkinModelType.NONE
    }
}

/**
 * 获取披风在本地的目标文件
 */
fun PlayerProfile.Cape.getFile(path: File): File {
    return File(path, "$id.png")
}