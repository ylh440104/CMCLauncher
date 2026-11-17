package com.cmc.launcher.game.version.installed.utils

import org.jackhuang.hmcl.util.versioning.GameVersionNumber

/**
 * 判断版本是否大于某个版本
 */
fun String.isBiggerVer(other: String): Boolean {
    return GameVersionNumber.compare(this, other) > 0
}

/**
 * 判断版本是否大于等于某个版本
 */
fun String.isBiggerOrEqualVer(other: String): Boolean {
    return GameVersionNumber.compare(this, other) >= 0
}

/**
 * 判断版本是否小于某个版本
 */
fun String.isLowerVer(other: String): Boolean {
    return GameVersionNumber.compare(this, other) < 0
}

/**
 * 判断版本是否小于等于某个版本
 */
fun String.isLowerOrEqualVer(other: String): Boolean {
    return GameVersionNumber.compare(this, other) <= 0
}