package com.cmc.launcher.ui.theme.cmc

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CmcHeroBanner(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    accent: Color = Cmc.Accent
) {
    val infinite = rememberInfiniteTransition(label = "cmc_hero")
    val shift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shift"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        accent.copy(alpha = 0.24f),
                        Cmc.Blue.copy(alpha = 0.16f),
                        Color.Transparent
                    ),
                    start = androidx.compose.ui.geometry.Offset(0f + shift * 420f, 0f),
                    end = androidx.compose.ui.geometry.Offset(760f - shift * 220f, 520f)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        accent.copy(alpha = 0.55f),
                        Cmc.Blue.copy(alpha = 0.28f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(accent, Cmc.Blue))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Cmc.SHORT.take(1),
                    color = Cmc.Ink,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CmcDot(
    active: Boolean,
    modifier: Modifier = Modifier,
    accent: Color = Cmc.Accent
) {
    val infinite = rememberInfiniteTransition(label = "cmc_dot")
    val alpha by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    Box(
        modifier = modifier
            .size(7.dp)
            .graphicsLayer { this.alpha = if (active) alpha else 0.28f }
            .clip(CircleShape)
            .background(if (active) accent else MaterialTheme.colorScheme.onSurfaceVariant)
    )
}

@Composable
fun CmcSectionDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        Cmc.Accent.copy(alpha = 0.34f),
                        Cmc.Blue.copy(alpha = 0.16f),
                        Color.Transparent
                    )
                )
            )
    )
}