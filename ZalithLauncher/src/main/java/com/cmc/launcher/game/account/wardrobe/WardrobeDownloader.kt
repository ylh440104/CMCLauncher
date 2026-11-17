package com.cmc.launcher.game.account.wardrobe

import com.google.gson.JsonObject
import com.cmc.launcher.utils.GSON
import com.cmc.launcher.utils.logging.Logger
import com.cmc.launcher.utils.network.downloadFile
import com.cmc.launcher.utils.network.fetchStringFromUrl
import com.cmc.launcher.utils.string.decodeBase64
import kotlinx.coroutines.CancellationException
import java.io.File

private const val TAG = "WardrobeDownloader"

abstract class WardrobeDownloader {
    protected suspend fun yggdrasil(
        url: String,
        uuid: String
    ): JsonObject {
        val profileJson = fetchStringFromUrl("${url.removeSuffix("/")}/session/minecraft/profile/$uuid")
        val profileObject = GSON.fromJson(profileJson, JsonObject::class.java)
        val properties = profileObject.get("properties").asJsonArray
        val rawValue = properties.get(0).asJsonObject.get("value").asString

        val value = decodeBase64(rawValue)

        return GSON.fromJson(value, JsonObject::class.java)
    }

    protected suspend fun download(url: String, file: File) {
        file.parentFile?.apply {
            if (!exists()) mkdirs()
        }

        try {
            downloadFile(url = url, outputFile = file)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            //皮肤获取失败并不致命，保持既有语义：记录日志而不上抛
            Logger.error(TAG, "Failed to download skin file", e)
        }
    }
}