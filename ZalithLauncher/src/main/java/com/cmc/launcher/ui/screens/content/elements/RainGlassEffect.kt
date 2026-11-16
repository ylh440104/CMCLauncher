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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.cmc.launcher.setting.AllSettings
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private const val GRAVITY = 9810f
private const val SURFACE_TENSION = 72.8f
private const val WATER_DENSITY = 0.001f
private const val WATER_VISCOSITY = 0.01002f
private const val PX_PER_MM = 6.0f

private const val TILT_SIN = 0.88f
private const val WIND_BASE = 0.9f

private const val THETA_E_RAD = 0.733f
private const val COS_THETA_A = 0.530f
private const val COS_THETA_R = 0.883f

private const val TRAIL_FADE = 0.035f

private fun hash21(x: Float, y: Float): Float {
    var h = x * 127.1f + y * 311.7f
    h = sin(h) * 43758.5453f
    return h - kotlin.math.floor(h)
}

private class GridDroplet {
    var gridX = 0
    var gridY = 0
    var rnd = 0f
    var x = 0f
    var y = 0f
    var rPx = 0f
    var hPx = 0f
    var volumeMm3 = 0f
    var born = 0f
    var sway = 0f
}

private class SliderDrop(
    var x: Float,
    var y: Float,
    var volumeMm3: Float,
    var velocity: Float = 0f,
    var swayPhase: Float = Random.nextFloat() * 6.28f,
    var tailT: Float = 0f
) {
    var dead = false
    var absorbed = false
}

private class TrailSeg(var x: Float, var y: Float, var wMm: Float, var wet: Float = 1f)

private class TrailPath {
    val segs = mutableListOf<TrailSeg>()
}

private class RainStreak(
    var x: Float = 0f,
    var y: Float = 0f,
    var vyPx: Float = 0f,
    var lenPx: Float = 0f,
    var widthPx: Float = 0f,
    var alpha: Float = 0f
)

private class SplashBit(
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var r: Float = 0f,
    var life: Float = 1f
)

private fun sphereCapGeom(volumeMm3: Float): Pair<Float, Float> {
    val denom = 2f - 3f * cos(THETA_E_RAD) + cos(THETA_E_RAD).pow3()
    val rmm = cbrt(volumeMm3 * 3f / (PI.toFloat() * denom))
    return rmm * sin(THETA_E_RAD) to rmm * (1f - cos(THETA_E_RAD))
}

private fun Float.pow3(): Float = this * this * this

private fun furmidgePinMdyn(volumeMm3: Float): Float {
    val (contactRMm, _) = sphereCapGeom(volumeMm3)
    return PI.toFloat() * 2f * contactRMm * SURFACE_TENSION * abs(COS_THETA_R - COS_THETA_A) / 10000f
}

private fun dropMassG(volumeMm3: Float): Float = volumeMm3 * WATER_DENSITY

@Composable
fun RainGlassEffect(
    modifier: Modifier = Modifier,
    enabled: Boolean = AllSettings.liquidGlass.state
) {
    var tick by remember { mutableFloatStateOf(0f) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var inited by remember { mutableStateOf(false) }

    val gridDrops = remember { mutableStateListOf<GridDroplet>() }
    val sliders = remember { mutableStateListOf<SliderDrop>() }
    val trails = remember { mutableStateListOf<TrailPath>() }
    val streaks = remember { mutableStateListOf<RainStreak>() }
    val splashes = remember { mutableStateListOf<SplashBit>() }
    var spawnAccum by remember { mutableFloatStateOf(0f) }
    var sliderAccum by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(enabled) {
        if (!enabled) return@LaunchedEffect
        var last = System.nanoTime()

        while (true) {
            delay(16)
            val w = canvasSize.width.toFloat()
            val h = canvasSize.height.toFloat()
            if (w <= 0f || h <= 0f) continue

            val now = System.nanoTime()
            val dt = min((now - last) / 1_000_000_000f, 0.05f)
            last = now
            if (dt <= 0f) continue

            if (!inited) {
                val cell = 46f
                val cols = (w / cell).toInt() + 1
                val rows = (h / cell).toInt() + 1
                for (cy in 0 until rows) {
                    for (cx in 0 until cols) {
                        val cellRnd = hash21(cx.toFloat() * 3.7f, cy.toFloat() * 7.3f)
                        if (cellRnd < 0.55f) continue
                        val vx = cx * cell + (hash21(cx.toFloat() * 11.1f, cy.toFloat() * 5.9f) - 0.5f) * cell * 0.7f
                        val vy = cy * cell + (hash21(cx.toFloat() * 2.3f, cy.toFloat() * 13.7f) - 0.5f) * cell * 0.7f
                        val vol = 1.2f + hash21(cx.toFloat() * 8.8f, cy.toFloat() * 4.4f) * 5.5f
                        val (crMm, chMm) = sphereCapGeom(vol)
                        gridDrops += GridDroplet().apply {
                            gridX = cx; gridY = cy
                            x = vx; y = vy
                            rPx = crMm * PX_PER_MM; hPx = chMm * PX_PER_MM
                            volumeMm3 = vol
                            rnd = hash21(cx.toFloat() * 1.7f, cy.toFloat() * 9.1f)
                            born = tick - rnd * 20f
                            sway = rnd * 6.28f
                        }
                    }
                }
                inited = true
            }

            spawnAccum += dt * 22f
            while (spawnAccum >= 1f) {
                spawnAccum -= 1f
                val dmm = 0.8f + 2.6f * Random.nextFloat()
                val vtMmps = 9650f - 10300f * exp(-0.6f * dmm)
                streaks += RainStreak(
                    x = Random.nextFloat() * w,
                    y = -30f,
                    vyPx = vtMmps * PX_PER_MM / 1000f,
                    lenPx = (vtMmps * PX_PER_MM / 1000f) * 0.045f,
                    widthPx = max(1.2f, dmm * PX_PER_MM * 0.28f),
                    alpha = 0.18f + Random.nextFloat() * 0.22f
                )
            }

            sliderAccum += dt * 1.6f
            while (sliderAccum >= 1f) {
                sliderAccum -= 1f
                sliders += SliderDrop(
                    x = Random.nextFloat() * w,
                    y = Random.nextFloat() * h * 0.8f,
                    volumeMm3 = 6f + Random.nextFloat() * 8f
                )
            }

            val iterator = streaks.listIterator()
            while (iterator.hasNext()) {
                val s = iterator.next()
                s.y += s.vyPx * dt
                s.x += (WIND_BASE * PX_PER_MM * 0.35f) * dt * sin(tick * 0.8f + s.y * 0.01f)
                if (s.y > h) iterator.remove()
            }

            sliders.forEach { d ->
                if (d.dead) return@forEach
                val massG = dropMassG(d.volumeMm3)
                val driveMdyn = massG * GRAVITY * TILT_SIN / 981f
                val pinMdyn = furmidgePinMdyn(d.volumeMm3)
                val netMdyn = max(driveMdyn - pinMdyn, 0f)
                val accelPx = netMdyn / max(massG, 1e-6f) * 981f * PX_PER_MM / 1000f
                d.velocity += accelPx * dt
                d.velocity = min(d.velocity, 260f + d.volumeMm3 * 6f)
                d.y += d.velocity * dt
                d.x += sin(tick * 1.6f + d.swayPhase) * 16f * dt
                d.tailT += dt

                val (contactRMm, _) = sphereCapGeom(d.volumeMm3)
                val ca = WATER_VISCOSITY * (d.velocity / PX_PER_MM) / SURFACE_TENSION
                val filmMm = 0.945f * contactRMm * max(ca, 1e-4f).powF(2f / 3f)
                val lostVol = 2f * contactRMm * filmMm * (d.velocity * dt / PX_PER_MM)
                d.volumeMm3 -= lostVol

                val trail = trails.lastOrNull { it.segs.size < 120 && it.segs.isNotEmpty() && it.segs.last().let { seg -> abs(seg.x - d.x) < 4f && seg.y < d.y } }
                if (trail == null) {
                    if (trails.size > 10) trails.removeAt(0)
                    trails += TrailPath()
                }
                trails.last().segs += TrailSeg(d.x, d.y, max(filmMm * PX_PER_MM * 8f, 3f))

                if (d.volumeMm3 < 1.2f || d.y > h + 40f) d.dead = true
            }
            sliders.removeAll { it.dead }

            gridDrops.removeAll { g ->
                sliders.any { s ->
                    !s.dead && abs(s.x - g.x) < g.rPx * 0.8f + s.volumeMm3.let { v -> sphereCapGeom(v).first * PX_PER_MM * 0.5f } && abs(s.y - g.y) < g.rPx * 0.8f + sphereCapGeom(s.volumeMm3).first * PX_PER_MM * 0.5f
                }.also {
                    if (it) {
                        sliders.firstOrNull { s -> abs(s.x - g.x) < 60f && abs(s.y - g.y) < 90f }?.let { s ->
                            s.volumeMm3 += g.volumeMm3
                        }
                    }
                }
            }

            splashes.removeAll { p ->
                p.life -= dt * 2.2f
                p.vy += GRAVITY * PX_PER_MM / 1000f * 0.6f * dt
                p.x += p.vx * dt
                p.y += p.vy * dt
                p.life <= 0f || p.y > h + 10f
            }

            trails.forEach { t ->
                val iter = t.segs.listIterator()
                while (iter.hasNext()) {
                    val seg = iter.next()
                    seg.wet -= TRAIL_FADE * dt
                    if (seg.wet <= 0f) iter.remove()
                }
            }
            trails.removeAll { it.segs.isEmpty() }

            tick += dt
        }
    }

    Canvas(modifier = modifier.onSizeChanged { canvasSize = it }) {
        if (!enabled || !inited) return@Canvas
        drawSheen(tick)
        drawTrailPaths(trails)
        drawStreaks(streaks)
        drawGridDroplets(gridDrops, tick)
        drawSliders(sliders)
        drawSplashBits(splashes)
    }
}

private fun Float.powF(e: Float): Float = Math.pow(this.toDouble(), e.toDouble()).toFloat()

private fun DrawScope.drawSheen(tick: Float) {
    val w = size.width
    val h = size.height
    val shift = sin(tick * 0.25f) * w * 0.12f
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.05f),
                Color.Transparent,
                Color(0xFFBFE3FF).copy(alpha = 0.04f),
                Color.Transparent
            ),
            start = Offset(shift, 0f),
            end = Offset(w + shift, h)
        )
    )
}

private fun DrawScope.drawStreaks(streaks: List<RainStreak>) {
    streaks.forEach { s ->
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = s.alpha * 0.5f),
                    Color.White.copy(alpha = s.alpha)
                ),
                start = Offset(s.x, s.y - s.lenPx),
                end = Offset(s.x, s.y)
            ),
            start = Offset(s.x, s.y - s.lenPx),
            end = Offset(s.x, s.y),
            strokeWidth = s.widthPx,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawGridDroplets(grid: List<GridDroplet>, tick: Float) {
    grid.forEach { g ->
        val pulse = 1f + sin(tick * 0.9f + g.sway) * 0.015f
        drawSingleDrop(
            cx = g.x,
            cy = g.y,
            rPx = g.rPx * pulse,
            hPx = g.hPx * pulse,
            stretch = 0f,
            tailW = 0f,
            velocity = 0f
        )
    }
}

private fun DrawScope.drawSliders(sliders: List<SliderDrop>) {
    sliders.forEach { d ->
        val (crMm, chMm) = sphereCapGeom(d.volumeMm3)
        drawSingleDrop(
            cx = d.x,
            cy = d.y,
            rPx = crMm * PX_PER_MM,
            hPx = chMm * PX_PER_MM,
            stretch = (d.velocity / 300f).coerceIn(0f, 1f),
            tailW = max(3f, crMm * PX_PER_MM * 0.7f),
            velocity = d.velocity
        )
    }
}

private fun DrawScope.drawSplashBits(splashes: List<SplashBit>) {
    splashes.forEach { p ->
        drawCircle(
            color = Color.White.copy(alpha = 0.55f * p.life),
            radius = p.r,
            center = Offset(p.x, p.y)
        )
    }
}

private fun DrawScope.drawTrailPaths(trails: List<TrailPath>) {
    trails.forEach { t ->
        var i = 0
        while (i < t.segs.size - 1) {
            val a = t.segs[i]
            val b = t.segs[i + 1]
            val alpha = (a.wet * 0.5f).coerceIn(0f, 0.75f)
            if (alpha > 0.03f) {
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFCBE4F8).copy(alpha = alpha * 0.5f),
                            Color(0xFFE9F5FF).copy(alpha = alpha)
                        ),
                        startY = a.y,
                        endY = b.y
                    ),
                    start = Offset(a.x, a.y),
                    end = Offset(b.x, b.y),
                    strokeWidth = a.wMm,
                    cap = StrokeCap.Round
                )
                if (a.wMm > 5f) {
                    drawLine(
                        color = Color.White.copy(alpha = alpha * 0.55f),
                        start = Offset(a.x - a.wMm * 0.26f, a.y),
                        end = Offset(b.x - b.wMm * 0.26f, b.y),
                        strokeWidth = max(0.8f, a.wMm * 0.13f),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFF7FA8CC).copy(alpha = alpha * 0.30f),
                        start = Offset(a.x + a.wMm * 0.28f, a.y),
                        end = Offset(b.x + b.wMm * 0.28f, b.y),
                        strokeWidth = max(0.8f, a.wMm * 0.13f),
                        cap = StrokeCap.Round
                    )
                }
            }
            i++
        }
    }
}

private fun DrawScope.drawSingleDrop(
    cx: Float,
    cy: Float,
    rPx: Float,
    hPx: Float,
    stretch: Float,
    tailW: Float,
    velocity: Float
) {
    val rw = max(rPx, 1.2f)
    val hh = max(hPx, 0.8f)
    val dome = Path().apply {
        moveTo(cx - rw, cy)
        cubicTo(cx - rw * 1.06f, cy - hh * 0.5f, cx - rw * 0.62f, cy - hh, cx, cy - hh)
        cubicTo(cx + rw * 0.62f, cy - hh, cx + rw * 1.06f, cy - hh * 0.5f, cx + rw, cy)
        close()
    }

    if (tailW > 0f && stretch > 0.02f) {
        val tailLen = 30f + 110f * stretch
        val tw = tailW
        drawPath(
            path = Path().apply {
                moveTo(cx - tw * 0.5f, cy - tailLen)
                cubicTo(cx - tw * 0.5f, cy - tailLen * 0.3f, cx - rw * 0.8f, cy - hh * 1.3f, cx - rw * 0.9f, cy - hh * 0.2f)
                lineTo(cx + rw * 0.9f, cy - hh * 0.2f)
                cubicTo(cx + tw * 0.5f, cy - tailLen * 0.3f, cx + tw * 0.5f, cy - tailLen * 0.3f, cx + tw * 0.5f, cy - tailLen)
                close()
            },
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFFC6E2F8).copy(alpha = 0.30f),
                    Color(0xFFE4F2FF).copy(alpha = 0.50f)
                ),
                startY = cy - tailLen,
                endY = cy
            )
        )
    }

    drawPath(
        path = dome,
        color = Color.Black.copy(alpha = 0.07f)
    )

    drawPath(
        path = dome,
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.88f),
                Color(0xFFDDF0FF).copy(alpha = 0.50f),
                Color(0xFFA5C9EC).copy(alpha = 0.32f),
                Color(0xFFEAF6FF).copy(alpha = 0.70f)
            ),
            center = Offset(cx - rw * 0.18f, cy - hh * 0.55f),
            radius = rw * 2.2f
        )
    )

    drawCircle(
        color = Color.White.copy(alpha = 0.92f),
        radius = max(0.8f, rw * 0.16f),
        center = Offset(cx - rw * 0.34f, cy - hh * 0.58f)
    )

    drawCircle(
        color = Color.White.copy(alpha = 0.30f),
        radius = max(0.5f, rw * 0.08f),
        center = Offset(cx + rw * 0.30f, cy - hh * 0.7f)
    )

    drawPath(
        path = dome,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.0f),
                Color.White.copy(alpha = 0.0f),
                Color(0xFF6E96B8).copy(alpha = 0.18f)
            ),
            start = Offset(cx, cy - hh),
            end = Offset(cx, cy)
        )
    )

    drawOvalSoft(cx, cy + hh * 0.05f, rw * 0.9f, rw * 0.32f, 0.28f)

    drawPath(
        path = Path().apply {
            moveTo(cx - rw, cy)
            cubicTo(cx - rw * 1.0f, cy - hh * 0.4f, cx - rw * 0.8f, cy - hh * 0.8f, cx - rw * 0.4f, cy - hh * 1.0f)
        },
        color = Color.White.copy(alpha = 0.35f),
        style = Stroke(width = max(0.7f, rw * 0.06f))
    )
    drawPath(
        path = Path().apply {
            moveTo(cx + rw * 0.4f, cy - hh * 0.95f)
            cubicTo(cx + rw * 0.85f, cy - hh * 0.6f, cx + rw, cy - hh * 0.3f, cx + rw, cy)
        },
        color = Color.White.copy(alpha = 0.22f),
        style = Stroke(width = max(0.6f, rw * 0.05f))
    )
}

private fun DrawScope.drawOvalSoft(cx: Float, cy: Float, rx: Float, ry: Float, alpha: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFEAF6FF).copy(alpha = alpha),
                Color(0xFFC0DDF4).copy(alpha = alpha * 0.5f),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = max(rx, ry)
        ),
        radius = max(rx, ry),
        center = Offset(cx, cy)
    )
}