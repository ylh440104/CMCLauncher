package com.cmc.launcher.game.account.wardrobe

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

/**
 * 皮肤模型枚举
 */
@Keep
@Parcelize
enum class SkinModelType(
    val string: String,
    val targetParity: Int,
    val modelType: String
): Parcelable {
    /** 未设定 */
    NONE("none", -1, ""),
    /** 粗臂类型 */
    STEVE("wide", 0, "classic"),
    /** 细臂类型 */
    ALEX("slim", 1, "slim")
}