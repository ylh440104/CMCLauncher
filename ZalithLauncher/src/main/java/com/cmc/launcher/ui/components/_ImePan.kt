package com.cmc.launcher.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

private class ImePanState {
    private var root by mutableStateOf<LayoutCoordinates?>(null)
    private var anchor by mutableStateOf<LayoutCoordinates?>(null)

    fun updateRoot(coordinates: LayoutCoordinates?) {
        root = coordinates
    }

    fun updateAnchor(coordinates: LayoutCoordinates?) {
        anchor = coordinates
    }

    fun isAnchor(coordinates: LayoutCoordinates?): Boolean = anchor === coordinates

    fun panOffsetY(imeHeightPx: Int): Int {
        val root = root ?: return 0
        val anchor = anchor ?: return 0
        val anchorBottom = root.localPositionOf(anchor, Offset(0f, anchor.size.height.toFloat())).y
        val visibleBottom = root.size.height - imeHeightPx
        return (anchorBottom - visibleBottom).coerceAtLeast(0f).roundToInt()
    }
}

private val LocalImePanState = staticCompositionLocalOf { ImePanState() }

@Composable
fun ImePanContainer(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = false,
    content: @Composable BoxWithConstraintsScope.() -> Unit
) {
    val state = remember { ImePanState() }
    CompositionLocalProvider(LocalImePanState provides state) {
        BoxWithConstraints(
            modifier = modifier.imePanRoot(),
            contentAlignment = contentAlignment,
            propagateMinConstraints = propagateMinConstraints,
            content = content
        )
    }
}

@Composable
fun Modifier.imePanRoot(): Modifier = composed {
    val state = LocalImePanState.current
    val density = LocalDensity.current
    val imeHeightPx = WindowInsets.ime.getBottom(density)

    val offsetY by animateFloatAsState(
        targetValue = state.panOffsetY(imeHeightPx).toFloat(),
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "imePanOffset"
    )

    this
        .onGloballyPositioned { state.updateRoot(it) }
        .offset { IntOffset(0, -offsetY.roundToInt()) }
}

@Composable
fun Modifier.imePanAnchor(): Modifier = composed {
    val state = LocalImePanState.current
    var isFocused by remember { mutableStateOf(false) }
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            if (state.isAnchor(coordinates)) state.updateAnchor(null)
        }
    }

    this
        .onGloballyPositioned {
            coordinates = it
            if (isFocused) state.updateAnchor(it)
        }
        .onFocusChanged { focus ->
            isFocused = focus.isFocused
            if (focus.isFocused) {
                state.updateAnchor(coordinates)
            } else if (state.isAnchor(coordinates)) {
                state.updateAnchor(null)
            }
        }
}