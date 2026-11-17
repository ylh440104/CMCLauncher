package com.cmc.launcher.game.version.multiplayer.description

import com.cmc.launcher.utils.string.stripColorCodes

/**
 * 以文本形式独立存储的服务器描述
 * @param value 文本信息
 */
data class StringDescription(
    val value: String
): ServerDescription {
    override fun toString(): String {
        return "StringDescription:\n${value.stripColorCodes()}"
    }
}