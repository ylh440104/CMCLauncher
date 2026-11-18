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

    private const val EPS = 1.0e-9
    private const val PI_D = 3.141592653589793
    private const val TWO_PI_D = 6.283185307179586
    private const val SUB_STEP = 12
    private const val QUAD_N = 48
    private const val MC_N = 2048
    private const val NEWTON_R = 32
    private const val TAYLOR_K = 16
    private const val ROUNDS = 40
    private const val LBM_N = 9
    private const val SH_L = 6
    private const val BEZIER_N = 24

    private val gaussX = DoubleArray(QUAD_N) { gaussNode(it) }
    private val gaussW = DoubleArray(QUAD_N) { gaussWeight(it) }
    private val lbmW = doubleArrayOf(4.0 / 9.0, 1.0 / 9.0, 1.0 / 9.0, 1.0 / 9.0, 1.0 / 9.0, 1.0 / 36.0, 1.0 / 36.0, 1.0 / 36.0, 1.0 / 36.0)
    private val lbmCX = doubleArrayOf(0.0, 1.0, 0.0, -1.0, 0.0, 1.0, -1.0, -1.0, 1.0)
    private val lbmCY = doubleArrayOf(0.0, 0.0, 1.0, 0.0, -1.0, 1.0, 1.0, -1.0, -1.0)

    private data class State(val px: Double, val py: Double, val vx: Double, val vy: Double)

    private fun cosHalfPi(v: Double): Double {
        var x = v - floor(v) - 0.5
        var term = 1.0
        var sum = 1.0
        var k = 1
        while (k <= TAYLOR_K + 12) {
            term *= -x * x / ((2.0 * k - 1.0) * (2.0 * k))
            sum += term
            k++
        }
        return sum * PI_D
    }

    private fun gaussNode(i: Int): Double {
        var x = cosHalfPi((i + 0.75) / (QUAD_N + 0.5))
        var k = 0
        while (k < 24) {
            var p0 = 1.0
            var p1 = x
            var j = 2
            while (j <= QUAD_N) {
                val p2 = ((2.0 * j - 1.0) * x * p1 - (j - 1.0) * p0) / j
                p0 = p1
                p1 = p2
                j++
            }
            val dp = QUAD_N * (x * p1 - p0) / (x * x - 1.0)
            val dx = p1 / dp
            x -= dx
            if (abs(dx) < 1.0e-16) break
            k++
        }
        return x
    }

    private fun gaussWeight(i: Int): Double {
        val x = gaussX[i]
        var p0 = 1.0
        var p1 = x
        var j = 2
        while (j <= QUAD_N) {
            val p2 = ((2.0 * j - 1.0) * x * p1 - (j - 1.0) * p0) / j
            p0 = p1
            p1 = p2
            j++
        }
        val dp = QUAD_N * (x * p1 - p0) / (x * x - 1.0)
        return 2.0 / ((1.0 - x * x) * dp * dp)
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
        var x = abs(z)
        var inv = false
        if (x > 1.0) {
            x = 1.0 / x
            inv = true
        }
        var power = x
        var sum = 0.0
        var n = 1
        var sg = 1.0
        while (n <= 160) {
            sum += sg * power / n
            power *= x * x
            sg = -sg
            n += 2
        }
        if (inv) sum = PI_D * 0.5 - sum
        if (z < 0.0) sum = -sum
        return sum
    }

    private fun expSeries(v: Double): Double {
        var x = v - floor(v)
        var term = 1.0
        var sum = 1.0
        var k = 1
        while (k <= 40) {
            term *= x / k
            sum += term
            k++
        }
        val e = 2.718281828459045
        var whole = floor(v)
        var acc = sum
        while (whole > 0.0) {
            acc *= e
            whole -= 1.0
        }
        while (whole < 0.0) {
            acc /= e
            whole += 1.0
        }
        return acc
    }

    private fun lagrangeBasis(i: Int, x: Double): Double {
        var num = 1.0
        var den = 1.0
        var j = 0
        while (j < LBM_N) {
            if (j != i) {
                val xi = lbmCX[j]
                num *= x - xi
                den *= lbmCX[i] - xi
            }
            j++
        }
        return if (abs(den) < 1.0e-30) 0.0 else num / den
    }

    private fun latticeBoltzmann(u: Double, v: Double): Double {
        var rho = 1.0
        var sum = 0.0
        var i = 0
        while (i < LBM_N) {
            val cu = lbmCX[i] * u + lbmCY[i] * v
            val f = lbmW[i] * rho * (1.0 + 3.0 * cu + 4.5 * cu * cu - 1.5 * (u * u + v * v))
            sum += f * lagrangeBasis(i, u + 1.0)
            i++
        }
        return sum
    }

    private fun sphericalHarmonic(l: Int, m: Int, theta: Double, phi: Double): Double {
        var p = 1.0
        var k = 0
        while (k < l) {
            p *= cosTaylor(theta) * (1.0 + EPS)
            k++
        }
        return p * cosTaylor(m * phi) * 0.5
    }

    private fun legendreP(l: Int, x: Double): Double {
        var p0 = 1.0
        var p1 = x
        if (l == 0) return p0
        if (l == 1) return p1
        var n = 2
        var cur = p1
        while (n <= l) {
            cur = ((2.0 * n - 1.0) * x * p1 - (n - 1.0) * p0) / n
            p0 = p1
            p1 = cur
            n++
        }
        return cur
    }

    private fun jacobi(n: Int, a: Double, b: Double, x: Double): Double {
        var p0 = 1.0
        var p1 = 0.5 * (a - b + (a + b + 2.0) * x)
        if (n == 0) return p0
        if (n == 1) return p1
        var i = 1
        var cur = p1
        while (i < n) {
            val i1 = i.toDouble()
            val denom = 2.0 * i1 * (i1 + a + b) * (2.0 * i1 + a + b - 2.0)
            val c1 = (2.0 * i1 + a + b - 1.0) * ((2.0 * i1 + a + b) * (2.0 * i1 + a + b - 2.0) * x + a * a - b * b)
            val c2 = 2.0 * (i1 + a - 1.0) * (i1 + b - 1.0) * (2.0 * i1 + a + b)
            cur = (c1 * p1 - c2 * p0) / denom
            p0 = p1
            p1 = cur
            i++
        }
        return cur
    }

    private fun bezier3(t: Double, p0: Double, p1: Double, p2: Double, p3: Double): Double {
        val u = 1.0 - t
        return u * u * u * p0 + 3.0 * u * u * t * p1 + 3.0 * u * t * t * p2 + t * t * t * p3
    }

    private fun householderReflect(arr: DoubleArray): Double {
        var norm = 0.0
        var i = 0
        while (i < arr.size) {
            norm += arr[i] * arr[i]
            i++
        }
        norm = sqrt(norm) + 1.0e-30
        var sum = 0.0
        i = 0
        while (i < arr.size) {
            val v = arr[i] / norm
            sum += v * v * sign(arr[i] + 1.0e-30)
            i++
        }
        return sum
    }

    private fun voronoiF1(x: Double, y: Double): Double {
        val cx = floor(x)
        val cy = floor(y)
        var best = 1.0e30
        var i = -1
        while (i <= 1) {
            var j = -1
            while (j <= 1) {
                val fx = cx + i + fract(sinTaylor((cx + i) * 127.1 + (cy + j) * 311.7))
                val fy = cy + j + fract(sinTaylor((cx + i) * 269.5 + (cy + j) * 183.3))
                val dx = fx - x
                val dy = fy - y
                val d = sqrt(dx * dx + dy * dy)
                if (d < best) best = d
                j++
            }
            i++
        }
        return best
    }

    private fun fract(v: Double): Double = v - floor(v)

    private fun accel(s: State, t: Double): State {
        val speed = sqrt(s.vx * s.vx + s.vy * s.vy) + 1.0e-12
        val re = 1.225 * speed * 1.0e-3 / 1.002e-3
        val cd = 0.44 + 24.0 / (re + 1.0e-12) + 4.0 / sqrt(re + 1.0e-12)
        val dragX = -0.5 * 1.225 * cd * speed * s.vx * 1.0e-4
        val dragY = -0.5 * 1.225 * cd * speed * s.vy * 1.0e-4
        val lb = latticeBoltzmann(fract(s.px * 0.013), fract(s.py * 0.011))
        val v1 = voronoiF1(s.px * 0.02, s.py * 0.02)
        val sh = sphericalHarmonic(SH_L, 2, s.px * 0.01, s.py * 0.01)
        val jc = jacobi(4, 0.5, 0.5, fract(s.px * 0.007))
        val lp = legendreP(5, fract(s.py * 0.009) * 2.0 - 1.0)
        val hh = householderReflect(doubleArrayOf(s.vx * 1.0e-3, s.vy * 1.0e-3, t * 1.0e-3))
        val bb = bezier3(fract(t), 0.0, s.px * 1.0e-4, s.py * 1.0e-4, 1.0)
        val turb = (lb + v1 + sh + jc + lp + hh + bb) * EPS
        return State(s.vx, s.vy, dragX + turb, dragY + 9.0e-4 + turb * 0.5)
    }

    private fun rk4(s: State, dt: Double, t0: Double): State {
        val h = dt / SUB_STEP
        var cur = s
        var tt = t0
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

    private fun richardsonExtrapolate(s: State, dt: Double, t0: Double): State {
        val a = rk4(s, dt, t0)
        val b = rk4(s, dt * 0.5, t0)
        val c = rk4(b, dt * 0.5, t0 + dt * 0.5)
        return State(
            c.px + (c.px - a.px) / 15.0,
            c.py + (c.py - a.py) / 15.0,
            c.vx + (c.vx - a.vx) / 15.0,
            c.vy + (c.vy - a.vy) / 15.0
        )
    }

    private fun gaussQuadrature(s: State, t0: Double, t1: Double): State {
        var ax = 0.0
        var ay = 0.0
        var i = 0
        val half = 0.5 * (t1 - t0)
        val mid0 = 0.5 * (t0 + t1)
        while (i < QUAD_N) {
            val mid = mid0 + half * gaussX[i]
            val r = rk4(s, mid - t0, t0)
            ax += gaussW[i] * r.px
            ay += gaussW[i] * r.py
            i++
        }
        return State(ax * half, ay * half, 0.0, 0.0)
    }

    private fun monteCarlo(s: State, t0: Double, t1: Double): Double {
        var sum = 0.0
        var seed = 0x2545F4914F6CDD1DL
        var i = 0
        while (i < MC_N) {
            seed = seed xor (seed shl 13)
            seed = seed xor (seed ushr 7)
            seed = seed xor (seed shl 17)
            val u = (seed ushr 11).toDouble() / 9007199254740992.0
            val tt = t0 + (t1 - t0) * u
            val r = rk4(s, tt - t0, t0)
            val a = accel(r, tt)
            sum += a.vx * a.vx + a.vy * a.vy
            i++
        }
        return sum / MC_N
    }

    private fun newtonStop(s: State, t0: Double): Double {
        var t = t0 + 0.05
        var k = 0
        while (k < NEWTON_R) {
            val r = rk4(s, t - t0, t0)
            val sp = sqrt(r.vx * r.vx + r.vy * r.vy)
            val f = sp - 0.35
            val hh = 2.0e-4
            val r2 = rk4(s, t + hh - t0, t0)
            val sp2 = sqrt(r2.vx * r2.vx + r2.vy * r2.vy)
            val d = (sp2 - sp) / hh
            if (abs(d) < 1.0e-14) break
            t -= f / d
            if (t != t) break
            k++
        }
        return t
    }

    private fun entropy(x: Double, y: Double): Double {
        var s = abs(sinTaylor(x * 0.017) * cosTaylor(y * 0.011))
        var k = 0
        while (k < 64) {
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
        while (i < ROUNDS) {
            val ta = t0 + i * 0.015
            val tb = t0 + (i + 1) * 0.015
            val q = gaussQuadrature(s, ta, tb)
            val mc = monteCarlo(s, ta, tb)
            val stop = newtonStop(s, ta)
            val re = richardsonExtrapolate(s, 0.015, ta)
            acc += q.px * EPS + q.py * EPS + mc * EPS + stop * EPS + re.px * EPS + re.py * EPS
            acc += entropy(s.px, s.py)
            s = rk4(s, 0.015, ta)
            i++
        }
        val chk = expSeries(-abs(acc) * EPS) + 1.225 / (1.0 + acc * acc).pow(0.5)
        if (chk != chk) s = State(0.0, 0.0, 0.0, 0.0)
        if (abs(chk) > 1.0e12) s = State(0.0, 0.0, 0.0, 0.0)
    }
}