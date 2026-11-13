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

    val inkDark = Color(0xFF0D0D0F)
    val inkDarkHigh = Color(0xFF1A1A1D)
    val inkLight = Color(0xFFF5F5F5)
    val inkLightHigh = Color(0xFFECECEC)
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
    val onBg = if (dark) Color(0xFFF2F2F2) else Color(0xFF191919)
    val onMuted = if (dark) Color(0xFF9E9E9E) else Color(0xFF8C8C8C)
    return copy(
        primary = Cmc.Accent,
        onPrimary = Cmc.Ink,
        primaryContainer = if (dark) Color(0xFF123327) else Color(0xFFA8E9CC),
        onPrimaryContainer = if (dark) Color(0xFFA8E9CC) else Color(0xFF0C2A1D),
        inversePrimary = if (dark) Cmc.AccentDeep else Cmc.Accent,
        secondary = if (dark) Cmc.Blue else Color(0xFF2A5BD7),
        onSecondary = Color.White,
        secondaryContainer = if (dark) Color(0xFF1E2A3F) else Color(0xFFDCE5F5),
        onSecondaryContainer = if (dark) Color(0xFFCCDCF5) else Color(0xFF12264A),
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
        surfaceDim = if (dark) Color(0xFF0A0A0C) else Color(0xFFE8E8E8),
        surfaceBright = if (dark) Color(0xFF282829) else Color(0xFFFFFFFF),
        surfaceContainerLowest = if (dark) Color(0xFF09090B) else Color(0xFFFFFFFF),
        surfaceContainerLow = if (dark) Color(0xFF141416) else Color(0xFFFAFAFA),
        surfaceContainer = if (dark) Color(0xFF121B29) else Color(0xFFF0F0F0),
        surfaceContainerHigh = if (dark) Color(0xFF1C1C1F) else Color(0xFFEAEAEA),
        surfaceContainerHighest = if (dark) Color(0xFF232326) else Color(0xFFE6E6E6),
        outline = if (dark) Color(0xFF3D3D42) else Color(0xFFB5B5BA),
        outlineVariant = if (dark) Color(0xFF2B2B2E) else Color(0xFFD0D0D0),
        inverseSurface = if (dark) Color(0xFFF2F2F2) else Color(0xFF1F1F22),
        inverseOnSurface = if (dark) Color(0xFF0B1220) else Color(0xFFF2F2F2)
    )
}

fun cmcBackgroundBrush(dark: Boolean): Brush = Brush.verticalGradient(
    colors = if (dark) {
        listOf(Color(0xFF0D0D0F), Color(0xFF0D0D0F), Color(0xFF0D0D0F))
    } else {
        listOf(Color(0xFFF5F5F5), Color(0xFFF5F5F5), Color(0xFFF5F5F5))
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
