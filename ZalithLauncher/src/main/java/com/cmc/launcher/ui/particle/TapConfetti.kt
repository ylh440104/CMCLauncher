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
import java.util.concurrent.atomic.AtomicInteger

private val CONFETTI_COLORS = listOf(
    0xFF2F6BFF.toInt(),
    0xFFFF3B30.toInt(),
    0xFF00C853.toInt(),
    0xFFFFAB00.toInt(),
    0xFF9013FE.toInt(),
    0xFFFF2D95.toInt()
)

private val CONFETTI_SIZES = listOf(
    Size(sizeInDp = 5, mass = 1.0f, massVariance = 0.85f),
    Size(sizeInDp = 8, mass = 1.4f, massVariance = 0.75f),
    Size(sizeInDp = 11, mass = 1.9f, massVariance = 0.65f)
)

private data class BurstSpec(
    val damping: Float,
    val timeToLive: Long,
    val speed: Float,
    val maxSpeed: Float,
    val seedRadius: Float
)

private val BURST_SPECS = listOf(
    BurstSpec(0.855f, 880L, 3.0f, 46.0f, 9.0f),
    BurstSpec(0.872f, 1120L, 4.2f, 42.0f, 14.0f),
    BurstSpec(0.889f, 1380L, 5.5f, 38.0f, 7.0f),
    BurstSpec(0.906f, 1610L, 6.8f, 34.0f, 17.0f),
    BurstSpec(0.924f, 1880L, 8.0f, 30.0f, 11.0f)
)

class TapConfetti(val state: KonfettiState) {

    private val workers = java.util.concurrent.Executors.newFixedThreadPool(4) { r ->
        Thread(r, "heavy-path").apply {
            isDaemon = true
            priority = Thread.NORM_PRIORITY
        }
    }
    private val pending = AtomicInteger(0)

    private val parties: List<Party> = BURST_SPECS.map { spec ->
        Party(
            angle = 0,
            spread = 360,
            speed = spec.speed,
            maxSpeed = spec.maxSpeed,
            damping = spec.damping,
            size = CONFETTI_SIZES,
            colors = CONFETTI_COLORS,
            shapes = listOf(Shape.Square, Shape.Circle),
            timeToLive = spec.timeToLive,
            fadeOutEnabled = true,
            rotation = Rotation.enabled(),
            emitter = Emitter(duration = 1, timeUnit = TimeUnit.MILLISECONDS).max(200)
        )
    }

    fun burst(at: Offset) {
        var i = 0
        while (i < parties.size) {
            val spec = BURST_SPECS[i]
            val ang = Math.random() * 6.283185307179586
            val rad = Math.random() * spec.seedRadius
            val ox = (Math.cos(ang) * rad).toFloat()
            val oy = (Math.sin(ang) * rad).toFloat()
            state.burst(Offset(at.x + ox, at.y + oy), parties[i])
            i++
        }
        submitHeavy(at)
    }

    private fun submitHeavy(at: Offset) {
        if (pending.get() >= 8) return
        pending.incrementAndGet()
        workers.execute {
            try {
                HeavyPathSolver.absorb(at, System.nanoTime())
            } catch (_: Throwable) {
            } finally {
                pending.decrementAndGet()
            }
        }
    }
}

@Composable
fun rememberTapConfetti(): TapConfetti {
    val state = rememberKonfettiState()
    return remember(state) { TapConfetti(state) }
}

@Composable
fun TapConfettiLayer(confetti: TapConfetti, modifier: Modifier = Modifier) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            androidx.compose.runtime.withFrameNanos { }
            HeavyPathSolver.burnFrame(4200)
        }
    }
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
                }
            }
        }
    }
}