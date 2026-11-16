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
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

private const val GRAVITY = 9.8f
private const val SURFACE_TENSION = 0.0728f
private const val WATER_DENSITY = 998.0f
private const val AIR_DENSITY = 1.204f
private const val WATER_VISCOSITY = 1.002e-3f
private const val SLIP_LENGTH = 10e-9f
private const val PX_PER_MM = 6.0f

private const val TILT_ANGLE_RAD = 1.05f
private const val WIND_SPEED = 1.1f
private const val WIND_TAU = 0.35f

private const val THETA_EQUIL_DEG = 42f
private const val THETA_ADV_DEG = 58f
private const val THETA_REC_DEG = 28f
private const val DEFECT_SCATTER = 0.3f

private const val SPLASH_THRESHOLD_K = 6500f
private const val TRAIL_FADE = 0.022f

private val COS_THETA_E = cos(Math.toRadians(THETA_EQUIL_DEG.toDouble())).toFloat()
private val COS_THETA_A = cos(Math.toRadians(THETA_ADV_DEG.toDouble())).toFloat()
private val COS_THETA_R = cos(Math.toRadians(THETA_REC_DEG.toDouble())).toFloat()

private class FallingDrop(
    var x: Float = 0f,
    var y: Float = 0f,
    var diameterMm: Float = 0f,
    var velocity: Float = 0f,
    var lifetime: Float = 0f,
    var landY: Float = 0f
) {
    fun terminalVelocity(): Float = 9.65f - 10.3f * exp(-0.6f * diameterMm)
}

private class SurfaceDrop(
    var x: Float = 0f,
    var y: Float = 0f,
    var volumeMm3: Float = 0f,
    var velocity: Float = 0f,
    var pinned: Boolean = true,
    var pinFactor: Float = 1f,
    var wobblePhase: Float = 0f,
    var age: Float = 0f,
    var spreadProgress: Float = 0f,
    var oscPhase: Float = 0f,
    var oscAmp: Float = 0f,
    var trailWidthMm: Float = 0f,
    var lastTrailX: Float = 0f,
    var lastTrailY: Float = 0f
) {
    fun radiusPx(): Float {
        val theta = THETA_EQUIL_DEG * PI.toFloat() / 180f
        val denom = 2f - 3f * cos(theta) + (cos(theta) * cos(theta) * cos(theta))
        val rmm = cbrt(volumeMm3 * 3f / (PI.toFloat() * denom))
        val contactR = rmm * sin(theta)
        return contactR * PX_PER_MM
    }

    fun heightPx(): Float {
        val theta = THETA_EQUIL_DEG * PI.toFloat() / 180f
        val denom = 2f - 3f * cos(theta) + (cos(theta) * cos(theta) * cos(theta))
        val rmm = cbrt(volumeMm3 * 3f / (PI.toFloat() * denom))
        val hMm = rmm * (1f - cos(theta))
        return hMm * PX_PER_MM
    }
}

private class SplashParticle(
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var diameterMm: Float = 0f,
    var life: Float = 1f,
    var gravityOn: Boolean = true
)

private class TrailSeg(
    var x: Float = 0f,
    var y: Float = 0f,
    var widthMm: Float = 0f,
    var wet: Float = 1f
)

private class TrailPath {
    val segs = mutableListOf<TrailSeg>()
    var dropId: Int = -1
}

private class StainMark(
    var x: Float = 0f,
    var y: Float = 0f,
    var rPx: Float = 0f,
    var wet: Float = 1f
)

private fun impactWeberNumber(velocityMps: Float, diameterMm: Float): Float {
    val d = diameterMm / 1000f
    return WATER_DENSITY * velocityMps * velocityMps * d / SURFACE_TENSION
}

private fun impactReynoldsNumber(velocityMps: Float, diameterMm: Float): Float {
    val d = diameterMm / 1000f
    return WATER_DENSITY * velocityMps * d / WATER_VISCOSITY
}

private fun mundoSplashParameter(velocityMps: Float, diameterMm: Float): Float {
    val we = impactWeberNumber(velocityMps, diameterMm)
    val re = impactReynoldsNumber(velocityMps, diameterMm)
    return we * sqrt(re)
}

private fun maxSpreadRatio(velocityMps: Float, diameterMm: Float): Float {
    val we = impactWeberNumber(velocityMps, diameterMm)
    val re = impactReynoldsNumber(velocityMps, diameterMm)
    val num = we + 12f
    val den = 3f * (1f - COS_THETA_E) + 4f * we / sqrt(re)
    return sqrt(num / max(den, 0.1f))
}

private fun furmidgePinForceMn(volumeMm3: Float, defect: Float): Float {
    val theta = THETA_EQUIL_DEG * PI.toFloat() / 180f
    val denom = 2f - 3f * cos(theta) + (cos(theta) * cos(theta) * cos(theta))
    val rmm = cbrt(volumeMm3 * 3f / (PI.toFloat() * denom))
    val contactRMm = rmm * sin(theta)
    val widthM = contactRMm * 2f / 1000f
    val base = PI.toFloat() * widthM * SURFACE_TENSION * abs(COS_THETA_R - COS_THETA_A)
    return base * defect
}

private fun voinovDynamicAngle(velocityMps: Float, contactRm: Float): Float {
    val logTerm = ln(max(contactRm / SLIP_LENGTH, 1.1f))
    val thetaE = THETA_EQUIL_DEG * PI.toFloat() / 180f
    val cube = thetaE * thetaE * thetaE + 9f * WATER_VISCOSITY * velocityMps * logTerm / SURFACE_TENSION
    return cbrt(max(cube, 1e-6f))
}

private fun huhScrivenDragNn(velocityMps: Float, contactRMm: Float): Float {
    val contactRm = contactRMm / 1000f
    val logTerm = ln(max(contactRm / SLIP_LENGTH, 1.1f))
    return 3.6f * WATER_VISCOSITY * velocityMps * contactRm * logTerm
}

private fun landauLevichFilmMm(contactRMm: Float, velocityMps: Float): Float {
    val ca = WATER_VISCOSITY * velocityMps / SURFACE_TENSION
    if (ca < 1e-7f) return 0.001f
    return 0.945f * contactRMm * ca.powF(2f / 3f)
}

private fun Float.powF(e: Float): Float = Math.pow(this.toDouble(), e.toDouble()).toFloat()

private fun windShearStress(): Float {
    return 0.5f * AIR_DENSITY * 0.005f * WIND_SPEED * WIND_SPEED
}

private fun evaporationRate(volumeMm3: Float): Float {
    val rmm = cbrt(volumeMm3 * 3f / (4f * PI.toFloat()))
    return 0.00045f * rmm
}

@Composable
fun RainGlassEffect(
    modifier: Modifier = Modifier,
    enabled: Boolean = AllSettings.liquidGlass.state
) {
    var tick by remember { mutableFloatStateOf(0f) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var inited by remember { mutableStateOf(false) }

    val falling = remember { mutableStateListOf<FallingDrop>() }
    val drops = remember { mutableStateListOf<SurfaceDrop>() }
    val splashes = remember { mutableStateListOf<SplashParticle>() }
    val trails = remember { mutableStateListOf<TrailPath>() }
    val stains = remember { mutableStateListOf<StainMark>() }
    var nextDropId by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(enabled) {
        if (!enabled) return@LaunchedEffect
        var last = System.nanoTime()
        var spawnAccum = 0f
        var windPhase = 0f

        while (true) {
            delay(16)
            val w = canvasSize.width.toFloat()
            val h = canvasSize.height.toFloat()
            if (w <= 0f || h <= 0f) continue

            val now = System.nanoTime()
            val dt = min((now - last) / 1_000_000_000f, 0.05f)
            last = now
            if (dt <= 0f) continue

            val windNow = WIND_SPEED * (0.6f + 0.4f * sin(windPhase * 0.7f))
            windPhase += dt
            spawnAccum += dt * 26f
            while (spawnAccum >= 1f) {
                spawnAccum -= 1f
                val dmm = 0.6f + 3.0f * (Random.nextFloat() * Random.nextFloat())
                falling += FallingDrop(
                    x = Random.nextFloat() * w,
                    y = -20f - Random.nextFloat() * 40f,
                    diameterMm = dmm,
                    velocity = 3.5f,
                    lifetime = 0f,
                    landY = Random.nextFloat() * h
                )
            }

            if (!inited) {
                repeat(14) {
                    drops += SurfaceDrop(
                        x = Random.nextFloat() * w,
                        y = Random.nextFloat() * h,
                        volumeMm3 = (0.5f + Random.nextFloat() * 2.5f) * 4.19f,
                        pinFactor = lerpF(1f - DEFECT_SCATTER, 1f + DEFECT_SCATTER, Random.nextFloat())
                    ).also { it.wobblePhase = Random.nextFloat() * 6.28f }
                }
                inited = true
            }

            val iterator = falling.listIterator()
            while (iterator.hasNext()) {
                val f = iterator.next()
                f.lifetime += dt
                val vt = f.terminalVelocity()
                val drag = (GRAVITY / (vt * vt)) * f.velocity * f.velocity
                f.velocity += (GRAVITY - drag) * dt
                f.y += f.velocity * PX_PER_MM * dt
                f.x += windNow * PX_PER_MM * 0.3f * dt

                if (f.y > f.landY) {
                    val vMps = f.velocity
                    val kParam = mundoSplashParameter(vMps, f.diameterMm)
                    if (kParam > SPLASH_THRESHOLD_K && splashes.size < 60) {
                        val spread = maxSpreadRatio(vMps, f.diameterMm)
                        val nBits = (2 + (kParam / 8000f).toInt()).coerceIn(2, 5)
                        repeat(nBits) {
                            splashes += SplashParticle(
                                x = f.x, y = f.landY,
                                vx = (Random.nextFloat() - 0.5f) * 4f * spread * PX_PER_MM,
                                vy = -(1.5f + Random.nextFloat() * 2.5f) * spread * PX_PER_MM * 0.6f,
                                diameterMm = f.diameterMm * (0.12f + Random.nextFloat() * 0.15f),
                                life = 1f
                            )
                        }
                        iterator.remove()
                    } else {
                        val newVol = (PI.toFloat() / 6f) * f.diameterMm * f.diameterMm * f.diameterMm
                        if (drops.size < 30) {
                            drops += SurfaceDrop(
                                x = f.x,
                                y = f.landY,
                                volumeMm3 = newVol,
                                pinFactor = lerpF(1f - DEFECT_SCATTER, 1f + DEFECT_SCATTER, Random.nextFloat())
                            ).also {
                                it.spreadProgress = 0f
                                it.oscAmp = min(0.35f, spreadOf(vMps, f.diameterMm) * 0.08f)
                                it.oscPhase = 0f
                            }
                        } else if (stains.size < 34) {
                            stains += StainMark(
                                x = f.x, y = f.landY,
                                rPx = f.diameterMm * 0.5f * PX_PER_MM * 1.4f,
                                wet = 0.9f
                            )
                        }
                        iterator.remove()
                    }
                }
            }

            drops.forEach { d ->
                d.age += dt
                if (d.spreadProgress < 1f) {
                    d.spreadProgress = min(1f, d.spreadProgress + dt * 5f)
                }
                if (d.oscAmp > 0.01f) {
                    d.oscPhase += dt * 9f
                    d.oscAmp *= (1f - dt * 1.2f)
                }

                d.volumeMm3 -= evaporationRate(d.volumeMm3) * dt
                if (d.volumeMm3 < 0.06f) {
                    if (Random.nextFloat() < 0.5f && stains.size < 34) {
                        stains += StainMark(
                            x = d.x, y = d.y,
                            rPx = max(3f, d.radiusPx() * 1.3f),
                            wet = 0.7f
                        )
                    }
                    d.x = Random.nextFloat() * w
                    d.y = -20f - Random.nextFloat() * 300f
                    d.volumeMm3 = (0.5f + Random.nextFloat() * 2.5f) * 4.19f
                    d.velocity = 0f
                    d.pinned = true
                    d.pinFactor = lerpF(1f - DEFECT_SCATTER, 1f + DEFECT_SCATTER, Random.nextFloat())
                    d.age = 0f
                    d.spreadProgress = 0f
                    d.oscAmp = 0f
                    d.trailWidthMm = 0f
                }

                val massKg = d.volumeMm3 * 1e-9f * WATER_DENSITY
                val driveN = massKg * GRAVITY * sin(TILT_ANGLE_RAD)
                val windN = windShearStress() * PI.toFloat() * (d.radiusPx() / PX_PER_MM / 1000f).let { it * it }
                val totalDrive = driveN + windN * 0.5f
                val pinN = furmidgePinForceMn(d.volumeMm3, d.pinFactor)

                if (d.pinned) {
                    if (totalDrive > pinN) {
                        d.pinned = false
                    }
                }

                if (!d.pinned) {
                    val contactRMm = d.radiusPx() / PX_PER_MM
                    val vMps = d.velocity / PX_PER_MM / 1000f
                    val dragN = huhScrivenDragNn(vMps, contactRMm)
                    val netN = totalDrive - pinN - dragN
                    val accelMps2 = netN / max(massKg, 1e-10f)
                    d.velocity += accelMps2 * PX_PER_MM / 1000f * dt
                    if (d.velocity < 0f) d.velocity = 0f

                    d.y += d.velocity * dt
                    d.x += (windNow * 8f + sin(tick * 1.5f + d.wobblePhase) * 12f) * dt

                    val filmMm = landauLevichFilmMm(contactRMm, d.velocity / PX_PER_MM / 1000f)
                    d.trailWidthMm = max(filmMm, 0.15f)
                    val lostVol = 2f * contactRMm * filmMm * (d.velocity * dt / PX_PER_MM)
                    d.volumeMm3 -= lostVol

                    val trail = trails.lastOrNull { it.dropId == d.hashCode() }
                    if (trail == null) {
                        if (trails.size > 14) trails.removeAt(0)
                        trails += TrailPath().also { it.dropId = d.hashCode() }
                    }
                    trails.last { it.dropId == d.hashCode() }.segs += TrailSeg().apply {
                        x = d.x
                        y = d.dropYTrail()
                        widthMm = d.trailWidthMm
                    }
                }
            }

            var i = 0
            while (i < drops.size) {
                var j = i + 1
                while (j < drops.size) {
                    val a = drops[i]
                    val b = drops[j]
                    val dx = a.x - b.x
                    val dy = a.y - b.y
                    val rr = (a.radiusPx() + b.radiusPx()) * 0.85f
                    if (dx * dx + dy * dy < rr * rr) {
                        val vol = a.volumeMm3 + b.volumeMm3
                        a.x = (a.x * a.volumeMm3 + b.x * b.volumeMm3) / vol
                        a.y = (a.y * a.volumeMm3 + b.y * b.volumeMm3) / vol
                        a.volumeMm3 = vol
                        a.pinned = true
                        a.velocity = 0f
                        a.oscAmp = 0.3f
                        a.oscPhase = 0f
                        drops.removeAt(j)
                    } else {
                        j++
                    }
                }
                i++
            }

            splashes.removeAll { p ->
                p.life -= dt * 1.7f
                p.vy += GRAVITY * PX_PER_MM * dt
                p.x += p.vx * dt
                p.y += p.vy * dt
                p.life <= 0f || p.y > canvasSize.height + 10f
            }

            stains.removeAll { s ->
                s.wet -= TRAIL_FADE * 0.5f * dt
                s.wet <= 0f
            }

            trails.forEach { t ->
                t.segs.removeAll { s ->
                    s.wet -= TRAIL_FADE * dt
                    s.wet <= 0f
                }
            }
            trails.removeAll { it.segs.isEmpty() }

            tick += dt
        }
    }

    Canvas(modifier = modifier.onSizeChanged { canvasSize = it }) {
        if (!enabled || !inited) return@Canvas
        drawAmbientSheen(tick)
        drawStainMarks(stains)
        drawTrailPaths(trails)
        drawFallingDrops(falling, tick)
        drawSplashParticles(splashes)
        drawSurfaceDropsFull(drops, tick)
    }
}

private fun spreadOf(vMps: Float, dmm: Float): Float = maxSpreadRatio(vMps, dmm)

private fun SurfaceDrop.dropYTrail(): Float = y + radiusPx() * 0.6f

private fun lerpF(a: Float, b: Float, t: Float) = a + (b - a) * t

private fun DrawScope.drawAmbientSheen(tick: Float) {
    val w = size.width
    val h = size.height
    val shift = sin(tick * 0.3f) * w * 0.15f
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.09f),
                Color.Transparent,
                Color(0xFFBFE3FF).copy(alpha = 0.06f),
                Color.Transparent
            ),
            start = Offset(shift, 0f),
            end = Offset(w + shift, h)
        )
    )
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.11f),
                Color.Transparent
            ),
            startY = 0f,
            endY = h * (0.26f + 0.05f * sin(tick * 0.4f))
        )
    )
}

private fun DrawScope.drawStainMarks(stains: List<StainMark>) {
    stains.forEach { s ->
        val alpha = s.wet * 0.35f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFE8F4FF).copy(alpha = alpha),
                    Color(0xFFC3DCF2).copy(alpha = alpha * 0.4f),
                    Color.Transparent
                ),
                center = Offset(s.x, s.y),
                radius = s.rPx
            ),
            radius = s.rPx,
            center = Offset(s.x, s.y)
        )
    }
}

private fun DrawScope.drawTrailPaths(trails: List<TrailPath>) {
    trails.forEach { t ->
        var i = 0
        while (i < t.segs.size - 1) {
            val a = t.segs[i]
            val b = t.segs[i + 1]
            val alpha = (a.wet * 0.62f).coerceIn(0f, 1f)
            if (alpha > 0.02f) {
                val wPx = a.widthMm * PX_PER_MM
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFD7EAFF).copy(alpha = alpha * 0.45f),
                            Color(0xFFEAF6FF).copy(alpha = alpha * 0.9f)
                        ),
                        startY = a.y,
                        endY = b.y
                    ),
                    start = Offset(a.x, a.y),
                    end = Offset(b.x, b.y),
                    strokeWidth = max(1.5f, wPx),
                    cap = StrokeCap.Round
                )
                if (wPx > 4f) {
                    drawLine(
                        color = Color.White.copy(alpha = alpha * 0.5f),
                        start = Offset(a.x - wPx * 0.25f, a.y),
                        end = Offset(b.x - b.widthMm * PX_PER_MM * 0.25f, b.y),
                        strokeWidth = max(0.8f, wPx * 0.14f),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFF86B0D6).copy(alpha = alpha * 0.3f),
                        start = Offset(a.x + wPx * 0.28f, a.y),
                        end = Offset(b.x + b.widthMm * PX_PER_MM * 0.28f, b.y),
                        strokeWidth = max(0.8f, wPx * 0.14f),
                        cap = StrokeCap.Round
                    )
                }
            }
            i++
        }
    }
}

private fun DrawScope.drawFallingDrops(falling: List<FallingDrop>, tick: Float) {
    falling.forEach { f ->
        val len = f.velocity * PX_PER_MM * 0.035f
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.25f),
                    Color.White.copy(alpha = 0.5f)
                ),
                start = Offset(f.x, f.y - len),
                end = Offset(f.x, f.y)
            ),
            start = Offset(f.x, f.y - len),
            end = Offset(f.x, f.y),
            strokeWidth = max(1.0f, f.diameterMm * PX_PER_MM * 0.4f),
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawSplashParticles(splashes: List<SplashParticle>) {
    splashes.forEach { p ->
        val rPx = p.diameterMm * 0.5f * PX_PER_MM
        drawCircle(
            color = Color.White.copy(alpha = 0.6f * p.life),
            radius = rPx,
            center = Offset(p.x, p.y)
        )
    }
}

private const val WATER_REFRACTIVE_INDEX = 1.333f
private const val FRESNEL_R0 = 0.0204f
private const val LIGHT_DIR_X = -0.62f
private const val LIGHT_DIR_Y = -0.78f

private fun fresnelReflectance(cosTheta: Float): Float {
    val c = cosTheta.coerceIn(0f, 1f)
    return FRESNEL_R0 + (1f - FRESNEL_R0) * (1f - c).powF(5f)
}

private fun causticGain(contactRMm: Float): Float {
    val fMm = WATER_REFRACTIVE_INDEX * contactRMm / (2f * (WATER_REFRACTIVE_INDEX - 1f))
    return (contactRMm / fMm).coerceIn(0f, 1f)
}

private fun DrawScope.drawSurfaceDropsFull(drops: List<SurfaceDrop>, tick: Float) {
    drops.forEach { d ->
        val rBase = d.radiusPx()
        val hPx = d.heightPx()
        val spread = d.spreadProgress * (1f + d.oscAmp * sin(d.oscPhase))
        val rw = rBase * spread.coerceIn(0.55f, 1.6f)
        val stretch = if (d.pinned) 0f else (d.velocity / (PX_PER_MM * 60f)).coerceIn(0f, 1.1f)
        val dome = dropDomePath(d.x, d.y, rw, hPx, stretch)

        val thicknessRatio = (hPx / max(rw, 1f)).coerceIn(0.15f, 1.2f)
        val edgeCos = (1f - thicknessRatio).coerceIn(0f, 1f)
        val rimReflect = fresnelReflectance(edgeCos)
        val caustic = causticGain(rw / PX_PER_MM)

        if (!d.pinned && d.trailWidthMm > 0.001f) {
            val tailLen = 24f + 90f * stretch
            drawPath(
                path = Path().apply {
                    moveTo(d.x - d.trailWidthMm * PX_PER_MM * 0.5f, d.y - tailLen)
                    cubicTo(
                        d.x - rw * 0.9f, d.y - hPx * 1.2f,
                        d.x - rw, d.y - hPx * 0.5f,
                        d.x - rw, d.y
                    )
                    lineTo(d.x + rw, d.y)
                    cubicTo(
                        d.x + rw, d.y - hPx * 0.5f,
                        d.x + rw * 0.9f, d.y - hPx * 1.2f,
                        d.x + d.trailWidthMm * PX_PER_MM * 0.5f, d.y - tailLen
                    )
                    close()
                },
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFCFE7FF).copy(alpha = 0.30f + rimReflect * 0.3f),
                        Color.White.copy(alpha = 0.45f + rimReflect * 0.4f)
                    ),
                    startY = d.y - tailLen,
                    endY = d.y
                )
            )
        }

        drawCircle(
            color = Color.Black.copy(alpha = 0.10f + thicknessRatio * 0.06f),
            radius = rw * (1.08f + thicknessRatio * 0.10f),
            center = Offset(d.x - LIGHT_DIR_X * rw * 0.14f, d.y + hPx * 0.10f)
        )

        drawPath(
            path = dome,
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFEBF7FF).copy(alpha = 0.14f + thicknessRatio * 0.10f),
                    Color(0xFFD6EBFF).copy(alpha = 0.34f),
                    Color(0xFF9FC4E8).copy(alpha = 0.40f + thicknessRatio * 0.12f),
                    Color(0xFFE6F3FF).copy(alpha = 0.55f + rimReflect * 0.35f)
                ),
                center = Offset(d.x - rw * 0.10f, d.y - hPx * 0.55f),
                radius = rw * 2.3f
            )
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFCCEDFF).copy(alpha = 0.55f * caustic + 0.15f),
                    Color(0xFFA8D4F0).copy(alpha = 0.30f * caustic),
                    Color.Transparent
                ),
                center = Offset(d.x + rw * 0.12f, d.y - hPx * 0.28f),
                radius = rw * 0.42f
            ),
            radius = rw * 0.42f,
            center = Offset(d.x + rw * 0.12f, d.y - hPx * 0.28f)
        )

        drawCircle(
            color = Color.White.copy(alpha = 0.80f + rimReflect * 0.15f),
            radius = max(1.0f, rw * 0.16f * (1f - stretch * 0.3f)),
            center = Offset(
                d.x + LIGHT_DIR_X * rw * 0.38f,
                d.y - hPx * (0.62f + stretch * 0.20f)
            )
        )

        drawCircle(
            color = Color.White.copy(alpha = 0.35f),
            radius = max(0.7f, rw * 0.09f),
            center = Offset(d.x + rw * 0.30f, d.y - hPx * 0.75f)
        )

        drawPath(
            path = dropDomePath(d.x, d.y, rw, hPx, stretch),
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Transparent,
                    Color(0xFF5E88A8).copy(alpha = 0.22f * thicknessRatio)
                ),
                start = Offset(d.x, d.y - hPx),
                end = Offset(d.x, d.y)
            )
        )

        drawPath(
            path = Path().apply {
                moveTo(d.x - rw, d.y)
                cubicTo(
                    d.x - rw * 1.02f, d.y - hPx * 0.5f,
                    d.x - rw * 0.8f, d.y - hPx * 0.85f,
                    d.x - rw * 0.45f, d.y - hPx * 1.05f
                )
            },
            color = Color.White.copy(alpha = 0.30f + rimReflect * 0.45f),
            style = Stroke(width = max(0.9f, rw * 0.07f))
        )

        drawPath(
            path = Path().apply {
                moveTo(d.x + rw * 0.55f, d.y - hPx * 0.85f)
                cubicTo(
                    d.x + rw * 0.92f, d.y - hPx * 0.55f,
                    d.x + rw * 1.02f, d.y - hPx * 0.25f,
                    d.x + rw, d.y
                )
            },
            color = Color.White.copy(alpha = 0.20f + rimReflect * 0.35f),
            style = Stroke(width = max(0.7f, rw * 0.05f))
        )

        drawOvalSoft(d.x, d.y + hPx * 0.06f, rw * 0.85f * caustic + 2f, rw * 0.3f, (0.30f + caustic * 0.30f).coerceAtMost(0.65f))

        drawPath(
            path = Path().apply {
                moveTo(d.x - rw * 0.12f, d.y - hPx * 0.80f)
                cubicTo(
                    d.x - rw * 0.42f, d.y - hPx * 0.45f,
                    d.x - rw * 0.28f, d.y - hPx * 0.18f,
                    d.x - rw * 0.10f, d.y - hPx * 0.06f
                )
            },
            color = Color.White.copy(alpha = 0.32f * (1f - caustic * 0.4f)),
            style = Stroke(width = max(0.6f, rw * 0.05f))
        )

        if (d.pinned && d.age < 0.25f) {
            val ring = 1f - d.age / 0.25f
            drawCircle(
                color = Color.White.copy(alpha = ring * 0.28f),
                radius = rw * (1.5f + (1f - ring) * 1.4f),
                center = Offset(d.x, d.y),
                style = Stroke(width = 1.5f)
            )
        }
    }
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

private fun dropDomePath(x: Float, y: Float, rPx: Float, hPx: Float, stretch: Float): Path = Path().apply {
    val h = hPx * (1f + stretch * 0.4f)
    val r = rPx * (1f - stretch * 0.15f)
    moveTo(x - r, y)
    cubicTo(x - r * 1.05f, y - h * 0.55f, x - r * 0.55f, y - h * 0.9f, x, y - h)
    cubicTo(x + r * 0.55f, y - h * 0.9f, x + r * 1.05f, y - h * 0.55f, x + r, y)
    close()
}