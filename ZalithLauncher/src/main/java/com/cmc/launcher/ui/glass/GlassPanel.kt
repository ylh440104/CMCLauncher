package com.cmc.launcher.ui.glass

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.glass.GlassOptics
import dev.chrisbanes.haze.glass.GlassStyle
import dev.chrisbanes.haze.glass.hazeGlass
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

class GlassHost(val state: HazeState)

@Composable
fun rememberGlassHost(): GlassHost {
    val state = rememberHazeState()
    return remember(state) { GlassHost(state) }
}

fun Modifier.glassSource(host: GlassHost): Modifier = this.hazeSource(host.state)

val LocalGlassHost = androidx.compose.runtime.staticCompositionLocalOf<GlassHost?> { null }

fun Modifier.glassPanelIf(host: GlassHost?, enabled: Boolean, fallback: Modifier): Modifier =
    if (host != null && enabled) this.glassPanel(host) else this.then(fallback)

private val PanelShape = RoundedCornerShape(26.dp)

private val panelStyleCache: GlassStyle by lazy {
    GlassStyle {
        optics(
            GlassOptics.regular.copy(
                refractionStrength = 0.95f,
                refractionDisplacement = 52.dp,
                refractionHeightFraction = 0.22f,
                refractionDetailIntensity = 0.85f
            )
        )
        shape(PanelShape)
        tint(Color.White.copy(alpha = 0.06f))
        specularIntensity(1.0f)
        specularExponent(48f)
        fresnelExponent(3.2f)
        edgeShadow(Color.Black.copy(alpha = 0.16f))
        edgeSoftness(1.6.dp)
        ambientResponse(0.55f)
        chromaticAberrationStrength(0.05f)
        contrast(0.10f)
        whitePoint(0.20f)
        chromaMultiplier(1.08f)
        contentNormalBlend(0.12f)
        alpha(0.94f)
    }
}

fun Modifier.glassPanel(host: GlassHost): Modifier = this.hazeGlass(
    input = HazeInput.Sources(host.state),
    style = panelStyleCache
)

fun Modifier.glassPanelSoft(host: GlassHost): Modifier = this.hazeGlass(
    input = HazeInput.Sources(host.state),
    style = softStyle
)

private val softStyle: GlassStyle by lazy {
    GlassStyle {
        optics(GlassOptics.clear)
        shape(RoundedCornerShape(20.dp))
        tint(Color.White.copy(alpha = 0.10f))
        specularIntensity(0.7f)
        alpha(0.90f)
    }
}