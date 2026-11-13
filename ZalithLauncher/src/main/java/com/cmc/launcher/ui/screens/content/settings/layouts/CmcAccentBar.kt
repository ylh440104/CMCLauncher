package com.cmc.launcher.ui.screens.content.settings.layouts

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cmc.launcher.ui.theme.cmc.Cmc

@Composable
fun CmcAccentBar(
    active: Boolean,
    modifier: Modifier = Modifier,
    accent: Color = Cmc.Accent
) {
    val color by animateColorAsState(
        targetValue = if (active) accent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
        animationSpec = tween(durationMillis = 220),
        label = "cmc_accent_bar"
    )

    Box(
        modifier = modifier
            .width(4.dp)
            .fillMaxHeight()
            .background(
                Brush.verticalGradient(
                    listOf(
                        color,
                        color.copy(alpha = if (active) 0.45f else 0.22f)
                    )
                )
            )
    )
}