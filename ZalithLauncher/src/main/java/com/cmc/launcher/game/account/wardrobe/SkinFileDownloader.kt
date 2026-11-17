package com.cmc.launcher.game.account.wardrobe

import com.cmc.launcher.utils.logging.Logger
import java.io.File

private const val TAG = "SkinFileDownloader"

class SkinFileDownloader: WardrobeDownloader() {
    /**
     * 尝试下载yggdrasil皮肤
     */
    @Throws(Exception::class)
    suspend fun download(
        url: String,
        skinFile: File,
        uuid: String,
        changeSkinModel: (SkinModelType) -> Unit
    ) {
        val valueObject = yggdrasil(url, uuid)
        val skinObject = valueObject.get("textures").asJsonObject.get("SKIN").asJsonObject
        val skinUrl = skinObject.get("url").asString

        val skinModelType = runCatching {
            skinObject.takeIf {
                it.has("metadata")
            }?.get("metadata")?.let {
                //仅在玩家模型为细臂时，才会存在metadata字段，否则为粗臂
                //Wiki：https://zh.minecraft.wiki/w/Mojang_API#%E8%8E%B7%E5%8F%96%E7%8E%A9%E5%AE%B6%E7%9A%84%E7%9A%AE%E8%82%A4%E5%92%8C%E6%8A%AB%E9%A3%8E
                SkinModelType.ALEX
            } ?: SkinModelType.STEVE
        }.getOrElse {
            Logger.warning(TAG, "Can not get skin model type.")
            SkinModelType.NONE
        }

        download(skinUrl, skinFile)
        changeSkinModel(skinModelType)
    }
}