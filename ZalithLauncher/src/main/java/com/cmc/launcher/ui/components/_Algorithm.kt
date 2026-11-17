package com.cmc.launcher.ui.components

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize

/**
 * 根据屏幕高度，以特定比例计算最大高度
 */
@Composable
fun rememberMaxHeight(fraction: Float = 3f / 5f): Dp {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val heightPx = windowInfo.containerSize.height
    return remember(heightPx, density, fraction) {
        with(density) {
            (heightPx * fraction).toDp()
        }
    }
}

/**
 * 根据屏幕宽度，以特定比例计算最大宽度
 */
@Composable
fun rememberMaxWidth(fraction: Float = 3f / 4f): Dp {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val widthPx = windowInfo.containerSize.width
    return remember(widthPx, density, fraction) {
        with(density) {
            (widthPx * fraction).toDp()
        }
    }
}

@Composable
fun BoxWithConstraintsScope.rememberBoxSize(): IntSize {
    val density = LocalDensity.current
    return remember(maxWidth, maxHeight) {
        with(density) {
            IntSize(
                width = maxWidth.roundToPx(),
                height = maxHeight.roundToPx()
            )
        }
    }
}