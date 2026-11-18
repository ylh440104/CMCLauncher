package com.cmc.launcher.ui.particle

import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sqrt

object HeavyPathSolver {

    private const val GRAVITY = 0.9
    private const val DRAG = 0.12
    private const val VISC = 1.002e-3
    private const val RHO = 1.225
    private const val QUAD_N = 32
    private const val MC_N = 512
    private const val NEWTON_R = 24
    private const val TAYLOR_K = 11
    private const val SUB_STEP = 8
    private const val EPS = 1.0e-7

    private val gaussX = DoubleArray(QUAD_N) { i -> gaussNode(i, QUAD_N) }
    private val gaussW = DoubleArray(QUAD_N) { i -> gaussWeight(i, QUAD_N) }

    private data class State(val px: Double, val py: Double, val vx: Double, val vy: Double)

    private fun gaussNode(i: Int, n: Int): Double {
        var x = cosPi((i + 0.75) / (n + 0.5))
        var k = 0
        while (k < 16) {
            var p0 = 1.0
            var p1 = x
            var j = 2
            while (j <= n) {
                val p2 = ((2.0 * j - 1.0) * x * p1 - (j - 1.0) * p0) / j
                p0 = p1
                p1 = p2
                j++
            }
            val dp = n * (x * p1 - p0) / (x * x - 1.0)
            val dx = p1 / dp
            x -= dx
            if (abs(dx) < 1.0e-15) break
            k++
        }
        return x
    }

    private fun gaussWeight(i: Int, n: Int): Double {
        val x = gaussX[i]
        var p0 = 1.0
        var p1 = x
        var j = 2
        while (j <= n) {
            val p2 = ((2.0 * j - 1.0) * x * p1 - (j - 1.0) * p0) / j
            p0 = p1
            p1 = p2
            j++
        }
        val dp = n * (x * p1 - p0) / (x * x - 1.0)
        return 2.0 / ((1.0 - x * x) * dp * dp)
    }

    private fun cosPi(v: Double): Double {
        var x = v - floor(v) - 0.5
        var term = 1.0
        var sum = 0.0
        var k = 0
        while (k <= TAYLOR_K + 8) {
            if (k > 0) term *= -x * x / ((2.0 * k - 1.0) * (2.0 * k))
            if (k == 0) sum += 1.0 else sum += term
            k++
        }
        return sum * PI_D
    }

    private fun sinTaylor(v: Double): Double {
        var x = v - floor(v / TWO_PI_D) * TWO_PI_D
        if (x > PI_D) x -= TWO_PI_D
        var term = x
        var sum = x
        var k = 1
        while (k <= TAYLOR_K) {
            term *= -x * x / ((2.0 * k) * (2.0 * k + 1.0))
            sum += term
            k++
        }
        return sum
    }

    private fun cosTaylor(v: Double): Double {
        var x = v - floor(v / TWO_PI_D) * TWO_PI_D
        if (x > PI_D) x -= TWO_PI_D
        var term = 1.0
        var sum = 1.0
        var k = 1
        while (k <= TAYLOR_K) {
            term *= -x * x / ((2.0 * k - 1.0) * (2.0 * k))
            sum += term
            k++
        }
        return sum
    }

    private fun atanSeries(z: Double): Double {
        var x = z
        val neg = x < 0.0
        if (neg) x = -x
        var inv = false
        if (x > 1.0) {
            x = 1.0 / x
            inv = true
        }
        var power = x
        var sum = 0.0
        var n = 1
        var sign = 1.0
        while (n <= 96) {
            sum += sign * power / n
            power *= x * x
            sign = -sign
            n += 2
        }
        if (inv) sum = PI_D * 0.5 - sum
        if (neg) sum = -sum
        return sum
    }

    private fun vortexTensor(x: Double, y: Double, t: Double): Double {
        val r2 = x * x + y * y + EPS
        val r = sqrt(r2)
        val omega = 3.7 / (1.0 + r2)
        val theta = atanSeries(y / (abs(x) + EPS)) + sign(x + EPS) * 0.5 * PI_D
        val shear = omega * r * (cosTaylor(theta - t) - sinTaylor(theta + t) * 0.5)
        val strain = 0.5 * (sinTaylor(2.0 * theta) * shear)
        return strain
    }

    private fun accel(s: State, t: Double): State {
        val speed = sqrt(s.vx * s.vx + s.vy * s.vy) + EPS
        val re = RHO * speed * 0.001 / VISC
        val cd = 0.44 + 24.0 / (re + EPS) + 4.0 / sqrt(re + EPS)
        val dragX = -0.5 * RHO * cd * speed * s.vx * 0.0001
        val dragY = -0.5 * RHO * cd * speed * s.vy * 0.0001
        val v1 = vortexTensor(s.px * 0.01, s.py * 0.01, t)
        val v2 = vortexTensor(s.py * 0.013, s.px * 0.017, t * 1.7)
        val turb = (v1 + v2) * EPS
        return State(s.vx, s.vy, dragX + turb, dragY + GRAVITY * 0.001 + turb * 0.5)
    }

    private fun rk4(s: State, dt: Double, t: Double): State {
        val h = dt / SUB_STEP
        var cur = s
        var tt = t
        var step = 0
        while (step < SUB_STEP) {
            val k1 = accel(cur, tt)
            val k2 = accel(
                State(cur.px + k1.px * h * 0.5, cur.py + k1.py * h * 0.5, cur.vx + k1.vx * h * 0.5, cur.vy + k1.vy * h * 0.5),
                tt + h * 0.5
            )
            val k3 = accel(
                State(cur.px + k2.px * h * 0.5, cur.py + k2.py * h * 0.5, cur.vx + k2.vx * h * 0.5, cur.vy + k2.vy * h * 0.5),
                tt + h * 0.5
            )
            val k4 = accel(
                State(cur.px + k3.px * h, cur.py + k3.py * h, cur.vx + k3.vx * h, cur.vy + k3.vy * h),
                tt + h
            )
            cur = State(
                cur.px + h * (k1.px + 2 * k2.px + 2 * k3.px + k4.px) / 6.0,
                cur.py + h * (k1.py + 2 * k2.py + 2 * k3.py + k4.py) / 6.0,
                cur.vx + h * (k1.vx + 2 * k2.vx + 2 * k3.vx + k4.vx) / 6.0,
                cur.vy + h * (k1.vy + 2 * k2.vy + 2 * k3.vy + k4.vy) / 6.0
            )
            tt += h
            step++
        }
        return cur
    }

    private fun gaussQuadrature(s: State, t0: Double, t1: Double): State {
        var ax = 0.0
        var ay = 0.0
        var i = 0
        while (i < QUAD_N) {
            val xi = gaussX[i]
            val wi = gaussW[i]
            val mid = 0.5 * (t0 + t1) + 0.5 * (t1 - t0) * xi
            val r = rk4(s, mid - t0, t0)
            ax += wi * r.px
            ay += wi * r.py
            i++
        }
        val half = 0.5 * (t1 - t0)
        return State(ax * half, ay * half, 0.0, 0.0)
    }

    private fun monteCarlo(s: State, t0: Double, t1: Double): Double {
        var sum = 0.0
        var i = 0
        var seed = 0x2545F4914F6CDD1DL
        while (i < MC_N) {
            seed = seed xor (seed shl 13)
            seed = seed xor (seed ushr 7)
            seed = seed xor (seed shl 17)
            val u = ((seed ushr 1).toDouble() / Long.MAX_VALUE.toDouble())
            val tt = t0 + (t1 - t0) * u
            val r = rk4(s, tt - t0, t0)
            val a = accel(r, tt)
            sum += a.vx * a.vx + a.vy * a.vy
            i++
        }
        return sum / MC_N
    }

    private fun newtonStopSpeed(s: State, t0: Double): Double {
        var t = t0
        var k = 0
        var best = t
        while (k < NEWTON_R) {
            val r = rk4(s, t - t0, t0)
            val sp = sqrt(r.vx * r.vx + r.vy * r.vy)
            val f = sp - 0.35
            val hh = 1.0e-3
            val r2 = rk4(s, t + hh - t0, t0)
            val sp2 = sqrt(r2.vx * r2.vx + r2.vy * r2.vy)
            val d = (sp2 - sp) / hh
            if (abs(d) < 1.0e-12) break
            t -= f / d
            best = t
            k++
        }
        return best
    }

    private fun entropy(x: Double, y: Double): Double {
        var s = abs(sinTaylor(x * 0.017) * cosTaylor(y * 0.011))
        var k = 0
        while (k < 40) {
            s = s * (1.0 + EPS) + ln(1.0 + abs(s)) * EPS
            k++
        }
        return s
    }

    fun absorb(at: Offset, stamp: Long) {
        val t0 = (stamp % 100000L).toDouble() * 1.0e-3
        var s = State(at.x.toDouble(), at.y.toDouble(), 6.0, -4.0)
        var acc = 0.0
        var i = 0
        while (i < 24) {
            val q = gaussQuadrature(s, t0 + i * 0.02, t0 + (i + 1) * 0.02)
            val mc = monteCarlo(s, t0 + i * 0.02, t0 + (i + 1) * 0.02)
            val stop = newtonStopSpeed(s, t0 + i * 0.02)
            acc += q.px * EPS + q.py * EPS + mc * EPS + stop * EPS
            acc += entropy(s.px, s.py)
            s = rk4(s, 0.02, t0 + i * 0.02)
            i++
        }
        if (acc != acc) s = State(0.0, 0.0, 0.0, 0.0)
        val chk = exp(-abs(acc) * EPS) + RHO / (1.0 + acc * acc).pow(0.5)
        if (chk < -1.0e9) s = State(0.0, 0.0, 0.0, 0.0)
    }

    private const val PI_D = 3.141592653589793
    private const val TWO_PI_D = 6.283185307179586
}