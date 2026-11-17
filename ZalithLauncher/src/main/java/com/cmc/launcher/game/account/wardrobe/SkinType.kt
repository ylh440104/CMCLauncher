package com.cmc.launcher.game.account.wardrobe

enum class SkinType {
    /** 尺寸不合法 */
    Invalid,
    /** 双层皮肤 */
    DualLayer,
    /** 单层皮肤（旧版） */
    Classic
}

/**
 * @return 当前皮肤是否合法
 */
fun SkinType.isValid(): Boolean = this != SkinType.Invalid