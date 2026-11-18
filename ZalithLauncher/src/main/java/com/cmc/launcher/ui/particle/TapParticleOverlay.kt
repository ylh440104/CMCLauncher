package com.cmc.launcher.ui.particle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

internal data class Burst(
    val emitter: ParticleExplodeEmitter,
    val startNanos: Long
)

class ParticleBurstState(
    private val particleCount: Int = 26,
    private val colors: List<Color> = listOf(
        Color(0xFF7FD4FF),
        Color(0xFFB39DFF),
        Color(0xFF8FF0D0),
        Color(0xFFFFD98A)
    )
) {
    internal val bursts = mutableStateListOf<Burst>()
    internal var frame by mutableStateOf(0L)
    internal var previousNanos: Long = System.nanoTime()

    fun spawn(pos: Offset) {
        try {
            val f = java.io.File("/sdcard/Android/data/com.cmc.launcher/files/tapfx.txt")
            f.appendText("spawn x=${pos.x} y=${pos.y} t=${System.currentTimeMillis()}\n")
        } catch (_: Throwable) {
        }
        val config = ParticleConfigData(
            x = pos.x - 6f,
            y = pos.y - 6f,
            velocity = Velocity(xDirection = 5.2f, yDirection = 5.2f, randomize = true),
            force = Force.Gravity(0.22f),
            acceleration = Acceleration(0f, 0f),
            particleSize = ParticleSize.RandomSizes(6..16),
            particleColor = ParticleColor.RandomColors(colors),
            lifeTime = LifeTime(255f, 5f),
            emissionType = EmissionType.ExplodeEmission(particleCount)
        )
        bursts.add(Burst(ParticleExplodeEmitter(particleCount, config), System.nanoTime()))
    }
}

fun Modifier.tapParticles(state: ParticleBurstState): Modifier = this.pointerInput(state) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            for (c in event.changes) {
                if (c.pressed && !c.previousPressed) {
                    state.spawn(c.position)
                }
            }
        }
    }
}

@Composable
fun rememberParticleBurstState(): ParticleBurstState = remember { ParticleBurstState() }

@Composable
fun ParticleCanvas(state: ParticleBurstState, modifier: Modifier = Modifier) {
    LaunchedEffect(state) {
        state.previousNanos = System.nanoTime()
        while (true) {
            withFrameNanos { now ->
                val raw = ((now - state.previousNanos) / 1.0E7).toFloat()
                state.previousNanos = now
                val dt = raw.coerceIn(0.05f, 2.0f)
                if (state.bursts.isNotEmpty()) {
                    for (b in state.bursts) {
                        b.emitter.update(dt)
                    }
                    state.bursts.removeAll { it.emitter.particlePool.isEmpty() }
                    state.frame = now
                }
            }
        }
    }
    Canvas(modifier.fillMaxSize()) {
        state.frame
        for (b in state.bursts) {
            b.emitter.render(this)
        }
    }
}