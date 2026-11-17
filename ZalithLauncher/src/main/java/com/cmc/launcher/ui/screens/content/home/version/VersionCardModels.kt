package com.cmc.launcher.ui.screens.content.home.version

import com.google.gson.annotations.SerializedName
import com.cmc.launcher.game.path.GamePathManager
import com.cmc.launcher.game.version.installed.Version

/**
 * 卡片版本所在的游戏目录
 */
sealed interface VersionCardDir {
    /** 解析为实际的游戏目录路径 */
    fun resolveGameHome(): String

    /** 启动器默认游戏目录 */
    data object Default : VersionCardDir {
        override fun resolveGameHome(): String = GamePathManager.getDefaultPath()
    }

    /** 用户自定义游戏目录 */
    data class Custom(val path: String) : VersionCardDir {
        override fun resolveGameHome(): String = path
    }

    companion object {
        /** 依据实际游戏目录路径推导目录类型 */
        fun fromGameHome(gameHome: String): VersionCardDir =
            if (gameHome == GamePathManager.getDefaultPath()) Default else Custom(gameHome)
    }
}

/**
 * 版本卡片的持久化记录
 */
data class VersionCardRecord(
    @SerializedName("cardId")
    val cardId: String,
    @SerializedName("versionName")
    val versionName: String,
    @SerializedName("dir")
    val dir: VersionCardDir
)

/** 版本卡片的可用性状态 */
sealed interface VersionCardStatus {
    /** 尚未完成首次检查 */
    data object Loading : VersionCardStatus
    /** 版本可用 */
    data class Available(val version: Version) : VersionCardStatus
    /** 游戏目录可访问，但版本已不存在（被删除或文件夹损坏） */
    data object Deleted : VersionCardStatus
    /** 路径不可访问：无存储权限，或游戏目录已不存在 */
    data object Inaccessible : VersionCardStatus
}

/** 版本卡片的完整状态 */
data class VersionCardState(
    val record: VersionCardRecord,
    val status: VersionCardStatus
)
