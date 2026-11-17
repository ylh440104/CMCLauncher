package com.cmc.launcher.game.version.mod

import android.os.Parcelable
import com.cmc.launcher.game.download.addon.platform.ModLoaderDisplayLabel
import com.cmc.launcher.game.download.addon.platform.Platform
import kotlinx.parcelize.Parcelize

/**
 * 模组在平台上对应的文件
 * @param id 文件ID
 * @param platform 所属平台
 * @param datePublished 发布日期
 */
@Parcelize
class ModFile(
    val id: String,
    val projectId: String,
    val platform: Platform,
    val loaders: Array<ModLoaderDisplayLabel>,
    val datePublished: String
): Parcelable