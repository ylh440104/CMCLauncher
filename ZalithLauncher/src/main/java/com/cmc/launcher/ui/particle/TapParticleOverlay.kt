package com.cmc.launcher.ui.particle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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

private data class Burst(
    val emitter: ParticleExplodeEmitter,
    val startNanos: Long
)

@Composable
fun TapParticleOverlay(
    modifier: Modifier = Modifier,
    particleCount: Int = 26,
    colors: List<Color> = listOf(
        Color(0xFF7FD4FF),
        Color(0xFFB39DFF),
        Color(0xFFFFFFFF),
        Color(0xFF8FF0D0)
    ),
    burstLifeNanos: Long = 1_100_000_000L
) {
    val bursts = remember { mutableStateListOf<Burst>() }
    var tick by remember { mutableStateOf(0L) }
    var previousNanos by remember { mutableStateOf(System.nanoTime()) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { now ->
                val dt = ((now - previousNanos) / 1.0E7).toFloat()
                previousNanos = now
                if (bursts.isNotEmpty()) {
                    val dead = ArrayList<Burst>()
                    for (b in bursts) {
                        b.emitter.update(dt)
                        if (b.emitter.particlePool.isEmpty()) dead.add(b)
                    }
                    if (dead.isNotEmpty()) bursts.removeAll { it in dead }
                    bursts.removeAll { now - it.startNanos > burstLifeNanos }
                    tick = now
                }
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Final)
                        for (c in event.changes) {
                            if (c.pressed && !c.previousPressed) {
                                spawnBurst(bursts, c.position, particleCount, colors)
                            }
                        }
                    }
                }
            }
    ) {
        @Suppress("UNUSED_EXPRESSION")
        tick
        for (b in bursts) {
            b.emitter.render(this)
        }
    }
}

private fun spawnBurst(
    bursts: MutableList<Burst>,
    pos: Offset,
    count: Int,
    colors: List<Color>
) {
    val config = ParticleConfigData(
        x = pos.x,
        y = pos.y,
        velocity = Velocity(xDirection = 26f, yDirection = 26f, randomize = true),
        force = Force.Gravity(0.55f),
        acceleration = Acceleration(0f, 0f),
        particleSize = ParticleSize.RandomSizes(2..7),
        particleColor = ParticleColor.RandomColors(colors),
        lifeTime = LifeTime(255f, 11f),
        emissionType = EmissionType.ExplodeEmission(count)
    )
    val emitter = ParticleExplodeEmitter(count, config)
    bursts.add(Burst(emitter, System.nanoTime()))
}