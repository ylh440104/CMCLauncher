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
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private const val SURFACE_TENSION = 0.0728f
private const val WATER_DENSITY = 1000f
private const val WATER_VISCOSITY = 0.001f
private const val GRAVITY = 9.8f
private const val TILT_SIN = 0.88f
private const val PX_PER_MM = 6f

private const val THETA_ADV_DEG = 56f
private const val THETA_REC_DEG = 36f
private const val PIN_SCATTER = 0.28f

private val COS_REC = cos(Math.toRadians(THETA_REC_DEG.toDouble())).toFloat()
private val COS_ADV = cos(Math.toRadians(THETA_ADV_DEG.toDouble())).toFloat()

private const val TRAIL_FADE = 0.020f
private const val DEPOSIT_RATE = 0.06f

private class RainStreak {
    var x = 0f
    var y = 0f
    var v = 0f
    var rPx = 0f

    fun reset(w: Float, randomY: Boolean) {
        x = Random.nextFloat() * w
        y = if (randomY) Random.nextFloat() * 2200f else -40f - Random.nextFloat() * 80f
        val dmm = lerpF(0.9f, 3.6f, Random.nextFloat() * Random.nextFloat())
        rPx = dmm * PX_PER_MM * 0.5f
        v = sqrt(1f + 4.5f * dmm) * 100f
    }
}

private class SurfaceDrop {
    var x = 0f
    var y = 0f
    var radiusPx = 0f
    var volumeMm3 = 0f
    var velocity = 0f
    var pinned = true
    var pinForce = 0f
    var wobble = 0f
    var age = 0f
    var trailWidth = 0f

    fun spawn(px: Float, py: Float, diameterMm: Float) {
        x = px
        y = py
        radiusPx = diameterMm * 0.5f * PX_PER_MM
        volumeMm3 = (PI.toFloat() / 6f) * diameterMm * diameterMm * diameterMm
        velocity = 0f
        pinned = true
        pinForce = retentionForce(radiusPx)
        wobble = Random.nextFloat() * 6.28f
        age = 0f
        trailWidth = 0f
    }
}

private class SplashBit {
    var x = 0f
    var y = 0f
    var vx = 0f
    var vy = 0f
    var r = 0f
    var life = 1f

    fun spawn(px: Float, py: Float, energy: Float) {
        x = px
        y = py
        vx = (Random.nextFloat() - 0.5f) * 300f * energy
        vy = -(80f + Random.nextFloat() * 240f) * energy
        r = 0.8f + Random.nextFloat() * 1.6f
        life = 0.5f + Random.nextFloat() * 0.4f
    }
}

private class Stain {
    var x = 0f
    var y = 0f
    var r = 0f
    var wet = 1f
}

private class TrailSeg {
    var x = 0f
    var y = 0f
    var w = 0f
    var wet = 1f
}

private class TrailPath {
    val segs = mutableListOf<TrailSeg>()
}

private fun lerpF(a: Float, b: Float, t: Float) = a + (b - a) * t

private fun Float.powF(e: Float): Float =
    Math.pow(this.toDouble(), e.toDouble()).toFloat()

private fun retentionForce(radiusPx: Float): Float {
    val widthMm = 2f * radiusPx / PX_PER_MM
    val widthM = widthMm / 1000f
    val scatter = lerpF(1f - PIN_SCATTER, 1f + PIN_SCATTER, Random.nextFloat())
    return PI.toFloat() * widthM * SURFACE_TENSION * abs(COS_REC - COS_ADV) * scatter
}

private fun dropMassKg(volumeMm3: Float): Float {
    val volumeM3 = volumeMm3 * 1e-9f
    return volumeM3 * WATER_DENSITY
}

private fun radiusPxFromVolume(volumeMm3: Float): Float {
    val rmm = ((volumeMm3 * 3f) / (4f * PI.toFloat())).powF(1f / 3f)
    return rmm * PX_PER_MM
}

private fun capillaryNumber(velocityPx: Float): Float {
    val vms = velocityPx / PX_PER_MM / 1000f
    return WATER_VISCOSITY * vms / SURFACE_TENSION
}

private fun landauLevichWidth(radiusPx: Float, velocityPx: Float): Float {
    val ca = capillaryNumber(velocityPx)
    if (ca < 1e-6f) return 2f
    val rmm = radiusPx / PX_PER_MM
    val hMm = 0.943f * rmm * ca.powF(2f / 3f) * 40f
    return max(2f, min(radiusPx * 1.15f, hMm))
}

@Composable
fun RainGlassEffect(
    modifier: Modifier = Modifier,
    enabled: Boolean = AllSettings.liquidGlass.state
) {
    var tick by remember { mutableFloatStateOf(0f) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var inited by remember { mutableStateOf(false) }

    val rain = remember { mutableStateListOf<RainStreak>().also { l -> repeat(40) { l += RainStreak() } } }
    val drops = remember { mutableStateListOf<SurfaceDrop>() }
    val splashes = remember { mutableStateListOf<SplashBit>() }
    val stains = remember { mutableStateListOf<Stain>() }
    val trails = remember { mutableStateListOf<TrailPath>() }

    LaunchedEffect(enabled) {
        if (!enabled) return@LaunchedEffect
        var last = System.nanoTime()
        while (true) {
            delay(16)
            val w = canvasSize.width.toFloat()
            val h = canvasSize.height.toFloat()
            if (w <= 0f || h <= 0f) continue
            if (!inited) {
                rain.forEach { it.reset(w, randomY = true) }
                repeat(18) {
                    drops += SurfaceDrop().also {
                        it.spawn(Random.nextFloat() * w, Random.nextFloat() * h, lerpF(1.0f, 2.4f, Random.nextFloat()))
                    }
                }
                inited = true
            }
            val now = System.nanoTime()
            val dt = min((now - last) / 1_000_000_000f, 0.05f)
            last = now
            if (dt <= 0f) continue

            drops.forEach { d ->
                d.age += dt

                val mass = dropMassKg(d.volumeMm3)
                val drive = mass * GRAVITY * TILT_SIN

                if (d.pinned) {
                    if (drive > d.pinForce) {
                        d.pinned = false
                    }
                }

                if (!d.pinned) {
                    val netN = drive - d.pinForce
                    val accelPx = (netN / max(mass, 1e-9f)) * PX_PER_MM / 1000f * 5f
                    d.velocity += accelPx * dt
                    d.velocity = min(d.velocity, 380f + d.radiusPx * 8f)
                    d.y += d.velocity * dt
                    d.x += sin(tick * 1.4f + d.wobble) * 9f * dt

                    val tw = landauLevichWidth(d.radiusPx, d.velocity)
                    d.trailWidth = tw
                    val trail = trails.lastOrNull()
                    if (trail == null || trail.segs.size >= 90) {
                        if (trails.size > 12) {
                            trails.removeAt(0)
                        }
                        trails += TrailPath()
                    }
                    trails.last().segs += TrailSeg().apply {
                        x = d.x
                        y = d.y
                        this.w = tw
                    }

                    d.volumeMm3 -= DEPOSIT_RATE * dt * (1f + d.velocity / 120f)
                    if (d.volumeMm3 > 0.02f) {
                        d.radiusPx = radiusPxFromVolume(d.volumeMm3)
                    }
                }

                if (d.volumeMm3 <= 0.02f || d.y > h + 30f) {
                    if (d.volumeMm3 <= 0.02f && Random.nextFloat() < 0.6f && stains.size < 30) {
                        stains += Stain().apply {
                            x = d.x
                            y = d.y.coerceAtMost(h - 6f)
                            r = max(4f, d.radiusPx * 1.5f)
                            wet = 0.8f
                        }
                    }
                    d.spawn(Random.nextFloat() * w, -20f - Random.nextFloat() * 200f, lerpF(1.0f, 2.8f, Random.nextFloat()))
                }
            }

            var di = 0
            while (di < drops.size) {
                val a = drops[di]
                var dj = di + 1
                var merged = false
                while (dj < drops.size) {
                    val b = drops[dj]
                    val dx = a.x - b.x
                    val dy = a.y - b.y
                    val rr = (a.radiusPx + b.radiusPx) * 0.9f
                    if (dx * dx + dy * dy < rr * rr) {
                        a.volumeMm3 += b.volumeMm3
                        a.x = (a.x + b.x) * 0.5f
                        a.y = (a.y + b.y) * 0.5f
                        a.radiusPx = radiusPxFromVolume(a.volumeMm3)
                        a.pinned = true
                        a.pinForce = retentionForce(a.radiusPx)
                        drops.removeAt(dj)
                        merged = true
                    } else {
                        dj++
                    }
                }
                di++
            }

            rain.forEach { fl ->
                fl.y += fl.v * dt
                if (fl.y > h - 4f) {
                    val energy = fl.rPx / 11f
                    if (fl.rPx > 5f && drops.size < 26 && Random.nextFloat() < 0.45f) {
                        drops += SurfaceDrop().also {
                            it.spawn(fl.x, h - fl.rPx * 2f, fl.rPx * 2f / PX_PER_MM)
                        }
                    } else if (splashes.size < 40 && Random.nextFloat() < 0.6f) {
                        repeat(2) {
                            splashes += SplashBit().also { it.spawn(fl.x, h - 4f, energy) }
                        }
                    }
                    if (fl.rPx > 6f && stains.size < 30 && Random.nextFloat() < 0.35f) {
                        stains += Stain().apply {
                            x = fl.x
                            y = h - 3f
                            r = fl.rPx * 1.7f
                            wet = 0.9f
                        }
                    }
                    fl.reset(w, randomY = false)
                }
            }

            splashes.removeAll { p ->
                p.life -= dt * 2.0f
                p.vy += 900f * dt
                p.x += p.vx * dt
                p.y += p.vy * dt
                p.life <= 0f || p.y > h + 10f
            }

            stains.removeAll { s ->
                s.wet -= TRAIL_FADE * 0.55f * dt
                s.wet <= 0f
            }

            trails.forEach { t ->
                t.segs.removeAll { s ->
                    s.wet -= TRAIL_FADE * dt
                    s.wet <= 0f
                }
            }

            tick += dt
        }
    }

    Canvas(modifier = modifier.onSizeChanged { canvasSize = it }) {
        if (!enabled || !inited) return@Canvas
        drawSheen(tick)
        drawStains(stains)
        drawTrails(trails)
        drawFallingRain(rain)
        drawSplashBits(splashes)
        drawSurfaceDrops(drops, tick)
    }
}

private fun DrawScope.drawSheen(tick: Float) {
    val w = size.width
    val h = size.height
    val shift = sin(tick * 0.30f) * w * 0.16f
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.10f),
                Color.Transparent,
                Color(0xFFBFE3FF).copy(alpha = 0.07f),
                Color.Transparent
            ),
            start = Offset(shift, 0f),
            end = Offset(w + shift, h)
        )
    )
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.12f),
                Color.Transparent
            ),
            startY = 0f,
            endY = h * (0.28f + 0.05f * sin(tick * 0.5f))
        )
    )
}

private fun DrawScope.drawStains(stains: List<Stain>) {
    stains.forEach { s ->
        drawOvalSoft(s.x, s.y, s.r * 1.25f, s.r * 0.55f, 0.38f * s.wet)
    }
}

private fun DrawScope.drawOvalSoft(cx: Float, cy: Float, rx: Float, ry: Float, alpha: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFE4F2FF).copy(alpha = alpha),
                Color(0xFFB9D6EF).copy(alpha = alpha * 0.45f),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = max(rx, ry)
        ),
        radius = max(rx, ry),
        center = Offset(cx, cy)
    )
}

private fun DrawScope.drawTrails(trails: List<TrailPath>) {
    trails.forEach { t ->
        var i = 0
        while (i < t.segs.size - 1) {
            val a = t.segs[i]
            val b = t.segs[i + 1]
            val alpha = (a.wet * 0.6f).coerceIn(0f, 1f)
            if (alpha > 0.02f) {
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFD7EAFF).copy(alpha = alpha * 0.4f),
                            Color(0xFFEAF6FF).copy(alpha = alpha * 0.85f)
                        ),
                        startY = a.y,
                        endY = b.y
                    ),
                    start = Offset(a.x, a.y),
                    end = Offset(b.x, b.y),
                    strokeWidth = min(a.w, b.w),
                    cap = StrokeCap.Round
                )
                if (a.w > 3.5f) {
                    drawLine(
                        color = Color.White.copy(alpha = alpha * 0.55f),
                        start = Offset(a.x - a.w * 0.26f, a.y),
                        end = Offset(b.x - b.w * 0.26f, b.y),
                        strokeWidth = max(0.8f, a.w * 0.14f),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFF8FB4D8).copy(alpha = alpha * 0.30f),
                        start = Offset(a.x + a.w * 0.28f, a.y),
                        end = Offset(b.x + b.w * 0.28f, b.y),
                        strokeWidth = max(0.8f, a.w * 0.14f),
                        cap = StrokeCap.Round
                    )
                }
            }
            i++
        }
    }
}

private fun DrawScope.drawFallingRain(rain: List<RainStreak>) {
    rain.forEach { fl ->
        val len = fl.v * 0.03f
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.30f),
                    Color.White.copy(alpha = 0.55f)
                ),
                start = Offset(fl.x, fl.y - len),
                end = Offset(fl.x, fl.y)
            ),
            start = Offset(fl.x, fl.y - len),
            end = Offset(fl.x, fl.y),
            strokeWidth = max(1.2f, fl.rPx * 0.5f),
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawSplashBits(splashes: List<SplashBit>) {
    splashes.forEach { p ->
        drawCircle(
            color = Color.White.copy(alpha = 0.65f * p.life),
            radius = p.r,
            center = Offset(p.x, p.y)
        )
    }
}

private fun teardropPath(x: Float, y: Float, r: Float, stretch: Float): Path = Path().apply {
    val rh = r * (1f + stretch * 0.35f)
    val rw = r * (1f - stretch * 0.20f)
    moveTo(x, y - rh * 2.05f)
    cubicTo(x + rw * 1.1f, y - rh * 0.7f, x + rw, y + rh * 0.15f, x + rw, y + rh * 0.55f)
    cubicTo(x + rw, y + rh * 1.4f, x + rw * 0.5f, y + rh * 1.65f, x, y + rh * 1.65f)
    cubicTo(x - rw * 0.5f, y + rh * 1.65f, x - rw, y + rh * 1.4f, x - rw, y + rh * 0.55f)
    cubicTo(x - rw, y + rh * 0.15f, x - rw * 1.1f, y - rh * 0.7f, x, y - rh * 2.05f)
    close()
}

private fun DrawScope.drawSurfaceDrops(drops: List<SurfaceDrop>, tick: Float) {
    drops.forEach { d ->
        val stretch = if (d.pinned) 0f else (d.velocity / 400f).coerceIn(0f, 1.1f)

        if (!d.pinned && d.trailWidth > 1f) {
            val tailLen = lerpF(26f, 86f, (d.velocity / 400f).coerceIn(0f, 1f))
            drawPath(
                path = Path().apply {
                    moveTo(d.x - d.trailWidth * 0.5f, d.y - tailLen)
                    cubicTo(
                        d.x - d.trailWidth * 0.5f, d.y - tailLen * 0.35f,
                        d.x - d.radiusPx * 0.85f, d.y - d.radiusPx * 1.2f,
                        d.x - d.radiusPx * 0.9f, d.y
                    )
                    lineTo(d.x + d.radiusPx * 0.9f, d.y)
                    cubicTo(
                        d.x + d.radiusPx * 0.85f, d.y - d.radiusPx * 1.2f,
                        d.x + d.trailWidth * 0.5f, d.y - tailLen * 0.35f,
                        d.x + d.trailWidth * 0.5f, d.y - tailLen
                    )
                    close()
                },
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFCFE7FF).copy(alpha = 0.38f),
                        Color.White.copy(alpha = 0.60f)
                    ),
                    startY = d.y - tailLen,
                    endY = d.y
                )
            )
        }

        val pulse = if (d.pinned) 1f + sin(tick * 1.2f + d.wobble) * 0.02f else 1f

        drawPath(
            path = teardropPath(d.x, d.y, d.radiusPx * pulse, stretch),
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.94f),
                    Color(0xFFDDF0FF).copy(alpha = 0.58f),
                    Color(0xFF93BADB).copy(alpha = 0.32f),
                    Color.White.copy(alpha = 0.78f)
                ),
                center = Offset(d.x - d.radiusPx * 0.2f, d.y - d.radiusPx * 0.35f),
                radius = d.radiusPx * 2.5f
            )
        )

        drawCircle(
            color = Color.White.copy(alpha = 0.95f),
            radius = d.radiusPx * 0.24f,
            center = Offset(d.x - d.radiusPx * 0.35f, d.y - d.radiusPx * 0.55f * (1f + stretch * 0.4f))
        )

        if (d.pinned && d.age < 0.3f) {
            val ring = 1f - d.age / 0.3f
            drawCircle(
                color = Color.White.copy(alpha = ring * 0.35f),
                radius = d.radiusPx * (1.4f + (1f - ring) * 1.6f),
                center = Offset(d.x, d.y),
                style = Stroke(width = 2f)
            )
        }
    }
}