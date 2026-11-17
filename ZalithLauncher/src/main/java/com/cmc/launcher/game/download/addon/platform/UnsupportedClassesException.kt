package com.cmc.launcher.game.download.addon.platform

/**
 * 不受支持的项目类别
 */
class UnsupportedClassesException(classes: Any?): IllegalArgumentException("Unsupported classes: $classes")