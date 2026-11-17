package com.cmc.launcher.utils.festival

import androidx.compose.runtime.compositionLocalOf

/**
 * 提供今天的节日信息
 */
val LocalFestivals = compositionLocalOf<List<Festival>> {
    error("No Festivals provided")
}