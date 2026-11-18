package com.cmc.launcher.ui.particle

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import nl.dionsegijn.konfetti.compose.KonfettiState
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.compose.rememberKonfettiState
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Rotation
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.core.models.Size
import java.util.concurrent.TimeUnit

class TapConfetti(val state: KonfettiState) {
    private val party = Party(
        angle = 0,
        spread = 360,
        speed = 12f,
        maxSpeed = 34f,
        damping = 0.88f,
        size = listOf(Size.SMALL, Size.MEDIUM, Size.LARGE),
        colors = listOf(
            0xFF2F6BFF.toInt(),
            0xFFFF3B30.toInt(),
            0xFF00C853.toInt(),
            0xFFFFAB00.toInt(),
            0xFF9013FE.toInt(),
            0xFFFF2D95.toInt()
        ),
        shapes = listOf(Shape.Square, Shape.Circle),
        timeToLive = 1400L,
        fadeOutEnabled = true,
        rotation = Rotation.enabled(),
        emitter = Emitter(duration = 1, timeUnit = TimeUnit.MILLISECONDS).max(96)
    )

    fun burst(at: Offset) {
        state.burst(at, party)
    }
}

@Composable
fun rememberTapConfetti(): TapConfetti {
    val state = rememberKonfettiState()
    return remember(state) { TapConfetti(state) }
}

@Composable
fun TapConfettiLayer(confetti: TapConfetti, modifier: Modifier = Modifier) {
    KonfettiView(
        state = confetti.state,
        modifier = modifier.fillMaxSize()
    )
}

fun Modifier.tapConfetti(confetti: TapConfetti): Modifier = this.pointerInput(confetti) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            for (c in event.changes) {
                if (c.pressed && !c.previousPressed) {
                    confetti.burst(c.position)
                    HeavyPathSolver.absorb(c.position, System.nanoTime())
                }
            }
        }
    }
}