package com.cmc.launcher.ui.screens.content.elements

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.cmc.launcher.setting.AllSettings
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private class RainStreak {
    var x = 0f
    var y = 0f
    var vy = 0f
    var length = 0f
    var width = 0f
    var alpha = 0f
    var drift = 0f

    fun reset(w: Float, h: Float, top: Boolean) {
        x = Random.nextFloat() * w
        y = if (top) -40f else Random.nextFloat() * h
        vy = 900f + Random.nextFloat() * 700f
        length = 34f + Random.nextFloat() * 46f
        width = 2f + Random.nextFloat() * 1.8f
        alpha = 0.35f + Random.nextFloat() * 0.30f
        drift = length * 0.10f
    }
}

private class GlassDroplet {
    var x = 0f
    var y = 0f
    var r = 0f
    var alpha = 0f
    var vy = 0f
    var phase = 0f

    fun reset(w: Float, h: Float) {
        x = Random.nextFloat() * w
        y = Random.nextFloat() * h
        r = 4f + Random.nextFloat() * 7f
        alpha = 0.55f + Random.nextFloat() * 0.30f
        vy = 4f + Random.nextFloat() * 14f
        phase = Random.nextFloat() * 6.28f
    }
}

private class TrickleDrop {
    var x = 0f
    var y = 0f
    var vy = 0f
    var r = 0f
    var alpha = 0f
    var tail = 0f
    var phase = 0f

    fun reset(w: Float, h: Float, fromTop: Boolean) {
        x = Random.nextFloat() * w
        y = if (fromTop) -20f else Random.nextFloat() * h
        vy = 70f + Random.nextFloat() * 90f
        r = 4.5f + Random.nextFloat() * 5f
        alpha = 0.60f + Random.nextFloat() * 0.25f
        tail = 26f + Random.nextFloat() * 60f
        phase = Random.nextFloat() * 6.28f
    }
}

private class Splash {
    var x = 0f
    var y = 0f
    var vx = 0f
    var vy = 0f
    var life = 1f
    var r = 0f

    fun spawn(px: Float, py: Float) {
        x = px
        y = py
        vx = (Random.nextFloat() - 0.5f) * 260f
        vy = -(120f + Random.nextFloat() * 220f)
        life = 0.7f + Random.nextFloat() * 0.3f
        r = 1.5f + Random.nextFloat() * 2.2f
    }
}

@Composable
fun RainGlassEffect(
    modifier: Modifier = Modifier,
    enabled: Boolean = AllSettings.liquidGlass.state
) {
    var tick by remember { mutableFloatStateOf(0f) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val streaks = remember { mutableStateListOf<RainStreak>().also { list -> repeat(55) { list += RainStreak() } } }
    val droplets = remember { mutableStateListOf<GlassDroplet>().also { list -> repeat(20) { list += GlassDroplet() } } }
    val trickles = remember { mutableStateListOf<TrickleDrop>().also { list -> repeat(14) { list += TrickleDrop() } } }
    val splashes = remember { mutableStateListOf<Splash>() }
    var inited by remember { mutableStateOf(false) }

    LaunchedEffect(enabled) {
        if (!enabled) return@LaunchedEffect
        var last = System.nanoTime()
        while (true) {
            delay(16)
            val w = canvasSize.width.toFloat()
            val h = canvasSize.height.toFloat()
            if (w <= 0f || h <= 0f) continue
            if (!inited) {
                streaks.forEach { it.reset(w, h, top = false) }
                droplets.forEach { it.reset(w, h) }
                trickles.forEach { it.reset(w, h, fromTop = false) }
                inited = true
            }
            val now = System.nanoTime()
            val dt = min((now - last) / 1_000_000_000f, 0.05f)
            last = now
            if (dt <= 0f) continue

            streaks.forEach { s ->
                s.y += s.vy * dt
                s.x -= s.drift * dt * 6f
                if (s.y - s.length > h) {
                    if (splashes.size < 40 && Random.nextFloat() < 0.55f) {
                        val sp = Splash()
                        sp.spawn(s.x.coerceIn(2f, w - 2f), h - 6f)
                        splashes += sp
                    }
                    s.reset(w, h, top = true)
                }
            }

            splashes.removeAll { p ->
                p.life -= dt * 2.4f
                p.vy += 1400f * dt
                p.x += p.vx * dt
                p.y += p.vy * dt
                p.life <= 0f || p.y > h + 10f
            }

            droplets.forEach { d ->
                d.y += d.vy * dt
                d.x += sin(tick * 0.9f + d.phase) * 6f * dt
                if (d.y > h + d.r * 2f) d.reset(w, h)
            }

            trickles.forEach { t ->
                t.x += sin(tick * 2.0f + t.phase) * 22f * dt
                t.y += t.vy * dt
                if (t.y - t.tail > h) t.reset(w, h, fromTop = true)
            }

            tick += dt
        }
    }

    Canvas(
        modifier = modifier.onSizeChanged { canvasSize = it }
    ) {
        if (!enabled || !inited) return@Canvas
        drawWetSheen(tick)
        drawStreaks(streaks)
        drawSplashes(splashes)
        drawDroplets(droplets)
        drawTrickles(trickles, tick)
    }
}

private fun DrawScope.drawWetSheen(tick: Float) {
    val w = size.width
    val h = size.height
    val shift = sin(tick * 0.35f) * w * 0.18f
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.12f),
                Color.Transparent,
                Color(0xFFBFE3FF).copy(alpha = 0.08f),
                Color.Transparent
            ),
            start = Offset(shift, 0f),
            end = Offset(w + shift, h)
        )
    )
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.14f),
                Color.Transparent
            ),
            startY = 0f,
            endY = h * (0.30f + 0.06f * sin(tick * 0.5f))
        )
    )
}

private fun DrawScope.drawStreaks(streaks: List<RainStreak>) {
    streaks.forEach { s ->
        val start = Offset(s.x + s.drift, s.y - s.length)
        val end = Offset(s.x - s.drift, s.y)
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = s.alpha * 0.5f),
                    Color.White.copy(alpha = s.alpha),
                    Color.White.copy(alpha = s.alpha * 0.85f)
                ),
                start = start,
                end = end
            ),
            start = start,
            end = end,
            strokeWidth = s.width,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawSplashes(splashes: List<Splash>) {
    splashes.forEach { p ->
        drawCircle(
            color = Color.White.copy(alpha = 0.55f * p.life),
            radius = p.r,
            center = Offset(p.x, p.y)
        )
    }
}

private fun dropPath(x: Float, y: Float, r: Float): Path = Path().apply {
    moveTo(x, y - r * 2.1f)
    cubicTo(x + r * 1.15f, y - r * 0.7f, x + r, y + r * 0.15f, x + r, y + r * 0.6f)
    cubicTo(x + r, y + r * 1.45f, x + r * 0.5f, y + r * 1.7f, x, y + r * 1.7f)
    cubicTo(x - r * 0.5f, y + r * 1.7f, x - r, y + r * 1.45f, x - r, y + r * 0.6f)
    cubicTo(x - r, y + r * 0.15f, x - r * 1.15f, y - r * 0.7f, x, y - r * 2.1f)
    close()
}

private fun DrawScope.drawWaterDrop(x: Float, y: Float, r: Float, alpha: Float, stretch: Float) {
    translate(left = r * 0.30f, top = r * 0.36f) {
        drawPath(
            path = dropPath(x, y, r * stretch),
            color = Color.Black.copy(alpha = alpha * 0.22f)
        )
    }

    drawPath(
        path = dropPath(x, y, r),
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = alpha),
                Color(0xFFDBEFFF).copy(alpha = alpha * 0.60f),
                Color(0xFF8FBFE8).copy(alpha = alpha * 0.34f),
                Color.White.copy(alpha = alpha * 0.90f)
            ),
            center = Offset(x, y - r * 0.2f),
            radius = r * 2.6f
        )
    )

    drawCircle(
        color = Color.White.copy(alpha = (alpha * 0.95f).coerceAtMost(1f)),
        radius = r * 0.30f,
        center = Offset(x - r * 0.38f, y - r * 0.55f)
    )

    drawCircle(
        color = Color.White.copy(alpha = alpha * 0.45f),
        radius = r * 0.14f,
        center = Offset(x + r * 0.32f, y + r * 1.05f)
    )

    drawLine(
        color = Color.White.copy(alpha = alpha * 0.35f),
        start = Offset(x - r * 0.35f, y + r * 1.55f),
        end = Offset(x + r * 0.35f, y + r * 1.55f),
        strokeWidth = r * 0.18f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawDroplets(droplets: List<GlassDroplet>) {
    droplets.forEach { d ->
        drawWaterDrop(d.x, d.y, d.r, d.alpha, 1f)
    }
}

private fun DrawScope.drawTrickles(trickles: List<TrickleDrop>, tick: Float) {
    trickles.forEach { t ->
        val speedT = (t.vy / 160f).coerceIn(0.4f, 1.4f)
        val wob = sin(tick * 2.0f + t.phase) * t.r * 0.4f

        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFFCFE8FF).copy(alpha = t.alpha * 0.35f),
                    Color.White.copy(alpha = t.alpha * 0.55f)
                ),
                startY = t.y - t.tail,
                endY = t.y
            ),
            start = Offset(t.x - wob, t.y - t.tail),
            end = Offset(t.x - wob * 0.2f, t.y),
            strokeWidth = t.r * 1.5f,
            cap = StrokeCap.Round
        )

        drawWaterDrop(t.x, t.y, t.r, t.alpha, speedT)
    }
}