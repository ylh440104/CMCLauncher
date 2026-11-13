package com.cmc.launcher.ui.theme.cmc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Cmc {
    const val NAME = "CMCLauncher"
    const val SHORT = "CMC"
    const val TAGLINE = "Minecraft Java Edition"

    val Accent = Color(0xFF4DE3A2)
    val AccentDeep = Color(0xFF0E7A55)
    val Blue = Color(0xFF4C8DFF)
    val Ink = Color(0xFF06120E)

    val inkDark = Color(0xFF0B1220)
    val inkDarkHigh = Color(0xFF16202F)
    val inkLight = Color(0xFFF3F7F4)
    val inkLightHigh = Color(0xFFE7EEE9)
}

val CmcShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

fun ColorScheme.cmcBrand(dark: Boolean): ColorScheme {
    val bg = if (dark) Cmc.inkDark else Cmc.inkLight
    val surfaceHigh = if (dark) Cmc.inkDarkHigh else Cmc.inkLightHigh
    val onBg = if (dark) Color(0xFFE6F1EA) else Color(0xFF101B15)
    val onMuted = if (dark) Color(0xFF9DB3A7) else Color(0xFF4A5A51)
    return copy(
        primary = Cmc.Accent,
        onPrimary = Cmc.Ink,
        primaryContainer = if (dark) Color(0xFF14402F) else Color(0xFFBDF3DC),
        onPrimaryContainer = if (dark) Color(0xFFBDF3DC) else Color(0xFF04301F),
        inversePrimary = if (dark) Cmc.AccentDeep else Cmc.Accent,
        secondary = if (dark) Cmc.Blue else Color(0xFF2A5BD7),
        onSecondary = Color.White,
        secondaryContainer = if (dark) Color(0xFF1B2B4A) else Color(0xFFD8E3FF),
        onSecondaryContainer = if (dark) Color(0xFFCBDCFF) else Color(0xFF0B2354),
        tertiary = if (dark) Color(0xFFF5C86B) else Color(0xFF8A5A00),
        onTertiary = Cmc.Ink,
        tertiaryContainer = if (dark) Color(0xFF3A2D0C) else Color(0xFFFFE6AE),
        onTertiaryContainer = if (dark) Color(0xFFFFE6AE) else Color(0xFF2E1F00),
        background = bg,
        onBackground = onBg,
        surface = bg,
        onSurface = onBg,
        surfaceVariant = surfaceHigh,
        onSurfaceVariant = onMuted,
        surfaceDim = if (dark) Color(0xFF070D16) else Color(0xFFE3EAE5),
        surfaceBright = if (dark) Color(0xFF22303F) else Color(0xFFFFFFFF),
        surfaceContainerLowest = if (dark) Color(0xFF060B12) else Color(0xFFFFFFFF),
        surfaceContainerLow = if (dark) Color(0xFF0F1724) else Color(0xFFF8FBF9),
        surfaceContainer = if (dark) Color(0xFF121B29) else Color(0xFFF0F5F2),
        surfaceContainerHigh = if (dark) Color(0xFF17212F) else Color(0xFFE9F0EB),
        surfaceContainerHighest = if (dark) Color(0xFF1D2836) else Color(0xFFE2EAE5),
        outline = if (dark) Color(0xFF3A4A58) else Color(0xFFB4C2B9),
        outlineVariant = if (dark) Color(0xFF283643) else Color(0xFFCEDAD2),
        inverseSurface = if (dark) Color(0xFFE6F1EA) else Color(0xFF16202F),
        inverseOnSurface = if (dark) Color(0xFF0B1220) else Color(0xFFE6F1EA)
    )
}

fun cmcBackgroundBrush(dark: Boolean): Brush = Brush.verticalGradient(
    colors = if (dark) {
        listOf(Color(0xFF0B1220), Color(0xFF0E1A24), Color(0xFF0B1220))
    } else {
        listOf(Color(0xFFF3F7F4), Color(0xFFEAF3EE), Color(0xFFF3F7F4))
    }
)

@Composable
fun CmcBrandChip(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Brush.horizontalGradient(listOf(Cmc.Accent, Cmc.Blue)))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = Cmc.SHORT,
            color = Cmc.Ink,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "LAUNCHER",
            color = Cmc.Ink.copy(alpha = 0.72f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 9.sp,
            letterSpacing = 1.2.sp
        )
    }
}
