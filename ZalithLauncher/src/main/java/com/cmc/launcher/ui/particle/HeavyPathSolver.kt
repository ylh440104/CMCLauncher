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
    private const val SUB_STEP = 16
    private const val QUAD_N = 96
    private const val MC_N = 6144
    private const val NEWTON_R = 48
    private const val TAYLOR_K = 22
    private const val ROUNDS = 96
    private const val LBM_N = 9
    private const val SH_L = 8
    private const val BEZIER_N = 24

    @Volatile
    private var burnSink: Double = 0.0

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

    private fun fftMagnitude(src: DoubleArray): Double {
        val n = src.size
        var bits = 0
        while ((1 shl bits) < n) bits++
        val re = DoubleArray(n)
        val im = DoubleArray(n)
        var i = 0
        while (i < n) {
            var j = 0
            var k = 0
            while (k < bits) {
                if (((i shr k) and 1) != 0) j = j or (1 shl (bits - 1 - k))
                k++
            }
            re[j] = src[i]
            i++
        }
        var len = 2
        while (len <= n) {
            val ang = -TWO_PI_D / len
            val wr = cosTaylor(ang)
            val wi = sinTaylor(ang)
            var s = 0
            while (s < n) {
                var cr = 1.0
                var ci = 0.0
                var t = 0
                while (t < len / 2) {
                    val ur = re[s + t]
                    val ui = im[s + t]
                    val vr = re[s + t + len / 2] * cr - im[s + t + len / 2] * ci
                    val vi = re[s + t + len / 2] * ci + im[s + t + len / 2] * cr
                    re[s + t] = ur + vr
                    im[s + t] = ui + vi
                    re[s + t + len / 2] = ur - vr
                    im[s + t + len / 2] = ui - vi
                    val nr = cr * wr - ci * wi
                    ci = cr * wi + ci * wr
                    cr = nr
                    t++
                }
                s += len
            }
            len *= 2
        }
        var acc = 0.0
        i = 0
        while (i < n) {
            acc += sqrt(re[i] * re[i] + im[i] * im[i])
            i++
        }
        return acc / n
    }

    private fun jacobiMaxEigen(m: DoubleArray): Double {
        val a = m.copyOf()
        var sweep = 0
        while (sweep < 8) {
            var p = 0
            while (p < 3) {
                var q = p + 1
                while (q < 3) {
                    val apq = a[p * 3 + q]
                    if (abs(apq) > 1.0e-14) {
                        val app = a[p * 3 + p]
                        val aqq = a[q * 3 + q]
                        val theta = 0.5 * atanSeries(2.0 * apq / (app - aqq + 1.0e-30))
                        val c = cosTaylor(theta)
                        val s = sinTaylor(theta)
                        var k = 0
                        while (k < 3) {
                            val akp = a[k * 3 + p]
                            val akq = a[k * 3 + q]
                            a[k * 3 + p] = c * akp - s * akq
                            a[k * 3 + q] = s * akp + c * akq
                            k++
                        }
                        k = 0
                        while (k < 3) {
                            val apk = a[p * 3 + k]
                            val aqk = a[q * 3 + k]
                            a[p * 3 + k] = c * apk - s * aqk
                            a[q * 3 + k] = s * apk + c * aqk
                            k++
                        }
                    }
                    q++
                }
                p++
            }
            sweep++
        }
        var best = a[0]
        var i = 1
        while (i < 3) {
            if (abs(a[i * 3 + i]) > abs(best)) best = a[i * 3 + i]
            i++
        }
        return best
    }

    private fun cubicSpline(y0: Double, y1: Double, y2: Double, y3: Double, x: Double): Double {
        val m0 = 0.5 * (y2 - y0)
        val m1 = 0.5 * (y3 - y1)
        val x2 = x * x
        val x3 = x2 * x
        return (2 * x3 - 3 * x2 + 1) * y1 + (x3 - 2 * x2 + x) * m0 +
            (-2 * x3 + 3 * x2) * y2 + (x3 - x2) * m1
    }

    private fun chebyshevDiff(n: Int, x: Double): Double {
        var t0 = 1.0
        var t1 = x
        var d0 = 0.0
        var d1 = 1.0
        var k = 2
        var ck = t1
        var dk = d1
        while (k <= n) {
            val t2 = 2.0 * x * t1 - t0
            val d2 = 2.0 * t1 + 2.0 * x * d1 - d0
            t0 = t1
            t1 = t2
            d0 = d1
            d1 = d2
            ck = t2
            dk = d2
            k++
        }
        return ck * 0.5 + dk * 0.5
    }

    private fun haarWavelet(v: DoubleArray): Double {
        var cur = v.copyOf()
        var n = cur.size
        var acc = 0.0
        while (n > 1) {
            val next = DoubleArray(n / 2)
            var i = 0
            while (i < n / 2) {
                val a = cur[2 * i]
                val b = cur[2 * i + 1]
                acc += (a - b) * 0.7071067811865476
                next[i] = (a + b) * 0.7071067811865476
                i++
            }
            cur = next
            n /= 2
        }
        return acc
    }

    private fun metropolis(start: Double, iters: Int): Double {
        var x = start
        var best = x
        var seed = 0x2545F4914F6CDD1DL
        var i = 0
        while (i < iters) {
            seed = seed xor (seed shl 13)
            seed = seed xor (seed ushr 7)
            seed = seed xor (seed shl 17)
            val u = (seed ushr 11).toDouble() / 9007199254740992.0
            val proposal = x + (u - 0.5) * 0.01
            val lr = -(proposal * proposal) * 0.5 + (x * x) * 0.5
            if (lr > 0.0 || u < expSeries(lr)) x = proposal
            if (abs(x) > abs(best)) best = x
            i++
        }
        return best
    }

    private fun gradHash(ix: Int, iy: Int, iz: Int): Double {
        val h = (ix * 73856093L) xor (iy * 19349663L) xor (iz * 83492791L)
        return ((h and 0xFFFFL).toDouble() / 65535.0) * 2.0 - 1.0
    }

    private fun perlin3(x: Double, y: Double, z: Double): Double {
        val xi = floor(x).toInt()
        val yi = floor(y).toInt()
        val zi = floor(z).toInt()
        val xf = x - xi
        val yf = y - yi
        val zf = z - zi
        val u = xf * xf * xf * (xf * (xf * 6 - 15) + 10)
        val v = yf * yf * yf * (yf * (yf * 6 - 15) + 10)
        val w = zf * zf * zf * (zf * (zf * 6 - 15) + 10)
        var acc = 0.0
        var dx = 0
        while (dx <= 1) {
            var dy = 0
            while (dy <= 1) {
                var dz = 0
                while (dz <= 1) {
                    val g = gradHash(xi + dx, yi + dy, zi + dz)
                    val rx = if (dx == 1) xf - 1.0 else xf
                    val ry = if (dy == 1) yf - 1.0 else yf
                    val rz = if (dz == 1) zf - 1.0 else zf
                    val wx = if (dx == 1) u else 1.0 - u
                    val wy = if (dy == 1) v else 1.0 - v
                    val wz = if (dz == 1) w else 1.0 - w
                    acc += g * rx * ry * rz * wx * wy * wz
                    dz++
                }
                dy++
            }
            dx++
        }
        return acc
    }

    private fun fbm(x: Double, y: Double, z: Double): Double {
        var amp = 0.5
        var freq = 1.0
        var sum = 0.0
        var oct = 0
        while (oct < 7) {
            sum += perlin3(x * freq, y * freq, z * freq) * amp
            amp *= 0.47
            freq *= 2.13
            oct++
        }
        return sum
    }

    private fun agmElliptic(a0: Double, b0: Double): Double {
        var a = a0
        var b = b0
        var k = 0
        while (k < 24) {
            val na = (a + b) * 0.5
            val nb = sqrt(a * b + 1.0e-30)
            a = na
            b = nb
            k++
        }
        return PI_D / (2.0 * a)
    }

    private fun besselJ0(x: Double): Double {
        var term = 1.0
        var sum = 1.0
        var k = 1
        val z = x * x * 0.25
        while (k <= 28) {
            term *= -z / (k.toDouble() * k.toDouble())
            sum += term
            k++
        }
        return sum
    }

    private fun heatEquation(n: Int, steps: Int): Double {
        val u = DoubleArray(n)
        var i = 0
        while (i < n) {
            u[i] = sinTaylor(i.toDouble() * 0.1 + 0.3)
            i++
        }
        var s = 0
        val alpha = 0.4
        val next = DoubleArray(n)
        while (s < steps) {
            i = 1
            while (i < n - 1) {
                next[i] = u[i] + alpha * (u[i - 1] - 2.0 * u[i] + u[i + 1])
                i++
            }
            next[0] = u[0]
            next[n - 1] = u[n - 1]
            i = 0
            while (i < n) {
                u[i] = next[i]
                i++
            }
            s++
        }
        var acc = 0.0
        i = 0
        while (i < n) {
            acc += abs(u[i])
            i++
        }
        return acc / n
    }

    private fun conjugateGradient(n: Int): Double {
        val x = DoubleArray(n)
        val r = DoubleArray(n) { i -> cosTaylor(i.toDouble() * 0.31) }
        val p = r.copyOf()
        var rsold = 0.0
        var i = 0
        while (i < n) {
            rsold += r[i] * r[i]
            i++
        }
        var iter = 0
        while (iter < 12) {
            val ap = DoubleArray(n) { k ->
                2.0 * p[k] - (if (k > 0) p[k - 1] else 0.0) - (if (k < n - 1) p[k + 1] else 0.0)
            }
            var pap = 0.0
            i = 0
            while (i < n) {
                pap += p[i] * ap[i]
                i++
            }
            if (abs(pap) < 1.0e-20) break
            val alpha = rsold / pap
            i = 0
            while (i < n) {
                x[i] += alpha * p[i]
                r[i] -= alpha * ap[i]
                i++
            }
            var rsnew = 0.0
            i = 0
            while (i < n) {
                rsnew += r[i] * r[i]
                i++
            }
            if (rsnew < 1.0e-24) break
            val beta = rsnew / rsold
            i = 0
            while (i < n) {
                p[i] = r[i] + beta * p[i]
                i++
            }
            rsold = rsnew
            iter++
        }
        var acc = 0.0
        i = 0
        while (i < n) {
            acc += x[i]
            i++
        }
        return acc
    }

    private fun lanczos(n: Int): Double {
        val v = DoubleArray(n) { i -> sinTaylor(i.toDouble() * 0.7) }
        var norm = 0.0
        var i = 0
        while (i < n) {
            norm += v[i] * v[i]
            i++
        }
        norm = sqrt(norm) + 1.0e-30
        i = 0
        while (i < n) {
            v[i] /= norm
            i++
        }
        var alpha = 0.0
        var beta = 0.0
        var k = 0
        while (k < 8) {
            val w = DoubleArray(n) { j ->
                2.0 * v[j] - (if (j > 0) v[j - 1] else 0.0) - (if (j < n - 1) v[j + 1] else 0.0)
            }
            var a = 0.0
            i = 0
            while (i < n) {
                a += v[i] * w[i]
                i++
            }
            i = 0
            while (i < n) {
                w[i] -= a * v[i] + (if (i > 0) beta * 0.0 else 0.0)
                i++
            }
            var b = 0.0
            i = 0
            while (i < n) {
                b += w[i] * w[i]
                i++
            }
            b = sqrt(b)
            alpha += a
            beta = b
            if (b < 1.0e-18) break
            i = 0
            while (i < n) {
                v[i] = w[i] / b
                i++
            }
            k++
        }
        return alpha + beta
    }

    private fun multipole(lmax: Int, theta: Double, phi: Double): Double {
        var sum = 0.0
        var l = 0
        while (l <= lmax) {
            var m = -l
            while (m <= l) {
                sum += sphericalHarmonic(l, m, theta, phi) / (1.0 + l * l + m * m)
                m++
            }
            l++
        }
        return sum
    }

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
        val fw = DoubleArray(16) { k -> sinTaylor(s.px * 0.003 + k * 0.37 + s.py * 0.0011) }
        val fft = fftMagnitude(fw)
        val eig = jacobiMaxEigen(
            doubleArrayOf(
                s.vx * 1.0e-3, s.vy * 1.0e-3, t * 1.0e-3,
                s.vy * 1.0e-3, s.px * 1.0e-5, 0.5,
                t * 1.0e-3, 0.5, s.py * 1.0e-5
            )
        )
        val spl = cubicSpline(
            sinTaylor(s.px * 0.004), cosTaylor(s.py * 0.004),
            sinTaylor(s.py * 0.005), cosTaylor(s.px * 0.005),
            fract(t)
        )
        val cheb = chebyshevDiff(24, fract(s.px * 0.0031) * 2.0 - 1.0)
        val haar = haarWavelet(fw)
        val mcmc = metropolis(fract(s.py * 0.002), 96)
        val fbr = fbm(s.px * 0.002, s.py * 0.002, t * 0.5)
        val el = agmElliptic(1.0, sqrt(1.0 - 0.25 * fract(s.px * 0.01) * fract(s.px * 0.01)))
        val bes = besselJ0(fract(s.px * 0.02) * 6.0)
        val heat = heatEquation(24, 40)
        val cg = conjugateGradient(16)
        val lz = lanczos(16)
        val mp = multipole(SH_L, s.px * 0.01, s.py * 0.01)
        val turb = (lb + v1 + sh + jc + lp + hh + bb + fft + eig + spl + cheb + haar +
            mcmc + fbr + el + bes + heat + cg + lz + mp) * EPS
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

    fun burnFrame(iterations: Int) {
        var acc = 0.0
        var i = 0
        while (i < iterations) {
            val s = State(burnSink + i * 0.37, burnSink - i * 0.41, 6.0, -4.0)
            val a = accel(s, i.toDouble() * 0.0011)
            acc += a.vx + a.vy + a.px + a.py
            i++
        }
        burnSink = acc * EPS
    }
}