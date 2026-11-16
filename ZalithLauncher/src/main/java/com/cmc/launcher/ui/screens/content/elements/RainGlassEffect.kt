package com.cmc.launcher.ui.screens.content.elements

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.cmc.launcher.setting.AllSettings
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private class RainDrop {
    var x = 0f
    var y = 0f
    var vy = 0f
    var length = 0f
    var width = 0f
    var alpha = 0f

    fun reset(w: Float, h: Float, top: Boolean) {
        x = Random.nextFloat() * w
        y = if (top) -length else Random.nextFloat() * h
        vy = 220f + Random.nextFloat() * 260f
        length = 14f + Random.nextFloat() * 22f
        width = 1f + Random.nextFloat() * 0.6f
        alpha = 0.10f + Random.nextFloat() * 0.18f
    }
}

private class TrickleDrop {
    var x = 0f
    var y = 0f
    var vy = 0f
    var r = 0f
    var alpha = 0f
    var tail = 0f

    fun reset(w: Float, h: Float) {
        x = Random.nextFloat() * w
        y = -10f
        vy = 26f + Random.nextFloat() * 40f
        r = 1.6f + Random.nextFloat() * 1.8f
        alpha = 0.14f + Random.nextFloat() * 0.20f
        tail = 10f + Random.nextFloat() * 26f
    }
}

@Composable
fun RainGlassEffect(
    modifier: Modifier = Modifier,
    enabled: Boolean = AllSettings.liquidGlass.state
) {
    var tick by remember { mutableFloatStateOf(0f) }
    var size by remember { mutableStateOf(Offset.Zero) }
    val drops = remember { mutableListOf<RainDrop>().apply { repeat(46) { RainDrop() } } }
    val trickles = remember { mutableListOf<TrickleDrop>().apply { repeat(12) { TrickleDrop() } } }
    var inited by remember { mutableStateOf(false) }

    LaunchedEffect(enabled, size) {
        if (!enabled || size == Offset.Zero) return@LaunchedEffect
        if (!inited && size.x > 0f) {
            drops.forEach { it.reset(size.x, size.y, top = true) }
            trickles.forEach { it.reset(size.x, size.y) }
            inited = true
        }
        var last = System.nanoTime()
        while (true) {
            delay(16)
            val now = System.nanoTime()
            val dt = min((now - last) / 1_000_000_000f, 0.05f)
            last = now
            if (dt <= 0f) continue
            drops.forEach { d ->
                d.y += d.vy * dt
                if (d.y > size.y) d.reset(size.x, size.y, top = true)
            }
            trickles.forEach { t ->
                val wobble = sin(tick * 2.2f + t.x * 0.05f) * 14f * dt
                t.x += wobble
                t.y += t.vy * dt
                if (t.y > size.y) t.reset(size.x, size.y)
            }
            tick += dt
        }
    }

    Canvas(
        modifier = modifier
    ) {
        if (!enabled || size == Offset.Zero) {
            size = Offset(this.size.width, this.size.height)
            return@Canvas
        }
        size = Offset(this.size.width, this.size.height)
        drawRain(drops, trickles)
    }
}

private fun DrawScope.drawRain(
    drops: List<RainDrop>,
    trickles: List<TrickleDrop>
) {
    drops.forEach { d ->
        drawLine(
            color = Color.White.copy(alpha = d.alpha),
            start = Offset(d.x, d.y),
            end = Offset(d.x, d.y - d.length),
            strokeWidth = d.width
        )
    }

    trickles.forEach { t ->
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = t.alpha),
                    Color.Transparent
                ),
                center = Offset(t.x, t.y),
                radius = t.r * 3f
            ),
            radius = t.r * 3f,
            center = Offset(t.x, t.y)
        )

        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = t.alpha * 0.7f)
                ),
                startY = t.y - t.tail,
                endY = t.y
            ),
            start = Offset(t.x, t.y - t.tail),
            end = Offset(t.x, t.y),
            strokeWidth = t.r * 1.2f
        )
    }
}