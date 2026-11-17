package com.cmc.launcher.game.version.installed

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.cmc.launcher.utils.GSON
import com.cmc.launcher.utils.logging.Logger
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "CurrentGameInfo"

/**
 * 当前游戏状态信息（支持旧配置迁移）
 * @property version 当前选择的版本名称
 * @property favoritesMap 收藏夹映射表 <收藏夹名称, 包含的版本集合>
 */
@Keep
data class CurrentGameInfo(
    @SerializedName("version")
    var version: String = "",
    @SerializedName("favoritesInfo")
    val favoritesMap: MutableMap<String, MutableSet<String>> = ConcurrentHashMap()
) {
    /**
     * 原子化保存当前状态到文件
     * @param gameHome 信息所属的游戏目录
     */
    fun saveCurrentInfo(gameHome: String) {
        val infoFile = getInfoFile(gameHome)
        runCatching {
            FileUtils.writeByteArrayToFile(infoFile, GSON.toJson(this).toByteArray(Charsets.UTF_8))
            Logger.debug(TAG, "Current version $version has been saved to the config file.")
        }.onFailure { e ->
            Logger.error(TAG, "Save failed: ${infoFile.absolutePath}", e)
        }
    }
}

private fun getInfoFile(gameHome: String) = File(gameHome, "cmc-game.cfg")

/**
 * 刷新并返回最新的游戏信息（自动处理旧配置迁移）
 * @param gameHome 信息所属的游戏目录
 */
fun refreshCurrentInfo(gameHome: String): CurrentGameInfo {
    val infoFile = getInfoFile(gameHome)

    return runCatching {
        when {
            infoFile.exists() -> loadFromJsonFile(infoFile)
            else -> createNewConfig(gameHome)
        }
    }.getOrElse { e ->
        Logger.error(TAG, "Refresh failed", e)
        createNewConfig(gameHome)
    }
}

private fun loadFromJsonFile(infoFile: File): CurrentGameInfo {
    return GSON.fromJson(infoFile.readText(), CurrentGameInfo::class.java).also { info ->
        checkNotNull(info) { "Deserialization returned null" }
    }
}

private fun createNewConfig(gameHome: String) = CurrentGameInfo().applyPostActions(gameHome)

private fun CurrentGameInfo.applyPostActions(gameHome: String): CurrentGameInfo {
    saveCurrentInfo(gameHome)
    return this
}