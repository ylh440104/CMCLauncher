package com.cmc.launcher.ui.screens.content.elements

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES31
import android.opengl.GLES32
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.Choreographer
import android.view.Surface
import android.view.View
import android.widget.FrameLayout
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class RainWindowOverlay @JvmOverloads constructor(
    context: Context,
    contentView: View? = null
) : FrameLayout(context) {

    private val glView = GLSurfaceView(context).apply {
        setEGLContextClientVersion(3)
        setZOrderOnTop(true)
        isClickable = false
        isFocusable = false
    }

    private val renderer = RainRenderer()

    private var uiSurfaceTexture: SurfaceTexture? = null
    private var uiSurface: Surface? = null
    private var capturedView: View? = null

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            capturedView?.let { capture(it) }
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    init {
        glView.setRenderer(renderer)
        glView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        capturedView = contentView
        contentView?.let {
            addView(it, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        }
        addView(glView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    fun attachOverlayContent(view: View) {
        capturedView = view
        addView(view, 0, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    override fun onDetachedFromWindow() {
        Choreographer.getInstance().removeFrameCallback(frameCallback)
        uiSurface?.release()
        uiSurface = null
        super.onDetachedFromWindow()
    }

    private fun capture(view: View) {
        val w = view.width
        val h = view.height
        if (w <= 0 || h <= 0) return
        val texId = renderer.uiTexId
        if (texId == 0) return

        if (uiSurfaceTexture == null) {
            try {
                uiSurfaceTexture = SurfaceTexture(texId).apply {
                    setDefaultBufferSize(w, h)
                    uiSurface = Surface(this)
                    renderer.attachSurfaceTexture(this)
                    Log.i("RainWindow", "SurfaceTexture created ${w}x$h}")
                }
            } catch (e: Throwable) {
                Log.e("RainWindow", "SurfaceTexture fail", e)
                return
            }
        }

        val surface = uiSurface ?: return
        if (!surface.isValid) return
        try {
            val canvas = surface.lockHardwareCanvas()
            view.draw(canvas)
            surface.unlockCanvasAndPost(canvas)
        } catch (e: Throwable) {
            Log.e("RainWindow", "capture fail", e)
        }
    }
}

private class RainRenderer : GLSurfaceView.Renderer {

    companion object {
        private const val TAG = "RainRenderer"
        private const val DROPS = 384
        private const val LOCAL = 64
        private const val HALF_W = 360
        private const val HALF_H = 806
    }

    @Volatile
    var uiTexId = 0
        private set

    @Volatile
    private var surfaceTexture: SurfaceTexture? = null

    private val stMatrix = FloatArray(16)
    private var matrixReady = false

    private var width = HALF_W * 2
    private var height = HALF_H * 2

    private var physicsProgram = 0
    private var filmProgram = 0
    private var compositeProgram = 0
    private var dropSsbo = 0
    private var filmA = 0
    private var filmB = 0
    private var flip = 0
    private var vao = 0
    private var lastNanos = System.nanoTime()
    private var elapsed = 0f

    fun attachSurfaceTexture(st: SurfaceTexture) {
        surfaceTexture = st
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val tex = IntArray(1)
        GLES32.glGenTextures(1, tex, 0)
        uiTexId = tex[0]
        GLES32.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, uiTexId)
        GLES32.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR)
        GLES32.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR)
        GLES32.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_CLAMP_TO_EDGE)
        GLES32.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_CLAMP_TO_EDGE)

        physicsProgram = buildCompute(PHYSICS_CS, "physics")
        filmProgram = buildCompute(FILM_PDE_CS, "film")
        compositeProgram = buildRender(FULLSCREEN_VS, COMPOSITE_FS, "composite")

        val vaos = IntArray(1)
        GLES32.glGenVertexArrays(1, vaos, 0)
        vao = vaos[0]

        initDrops()
        Log.i(TAG, "GL init done")
    }

    override fun onSurfaceChanged(gl: GL10?, w: Int, h: Int) {
        width = max(1, w)
        height = max(1, h)
        surfaceTexture?.setDefaultBufferSize(width, height)
        initFilmTextures()
        Log.i(TAG, "surface ${width}x${height}")
    }

    override fun onDrawFrame(gl: GL10?) {
        val now = System.nanoTime()
        val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0.0005f, 0.05f)
        lastNanos = now
        elapsed += dt

        surfaceTexture?.let {
            try {
                it.updateTexImage()
                it.getTransformMatrix(stMatrix)
                matrixReady = true
            } catch (_: Throwable) {
            }
        }

        val filmIn = if (flip == 0) filmA else filmB
        val filmOut = if (flip == 0) filmB else filmA

        GLES32.glUseProgram(physicsProgram)
        GLES32.glBindBufferBase(GLES32.GL_SHADER_STORAGE_BUFFER, 0, dropSsbo)
        GLES32.glBindImageTexture(1, filmOut, 0, false, 0, GLES32.GL_READ_WRITE, GLES32.GL_RGBA16F)
        u2(physicsProgram, "uSim", width.toFloat(), height.toFloat())
        u1(physicsProgram, "uDt", dt)
        u1(physicsProgram, "uTime", elapsed)
        GLES32.glDispatchCompute(DROPS / LOCAL, 1, 1)
        GLES32.glMemoryBarrier(GLES32.GL_SHADER_STORAGE_BARRIER_BIT or GLES32.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT)

        GLES32.glUseProgram(filmProgram)
        GLES32.glBindImageTexture(0, filmIn, 0, false, 0, GLES32.GL_READ_ONLY, GLES32.GL_RGBA16F)
        GLES32.glBindImageTexture(1, filmOut, 0, false, 0, GLES32.GL_READ_WRITE, GLES32.GL_RGBA16F)
        u1(filmProgram, "uDt", dt)
        GLES32.glDispatchCompute((width + 15) / 16, (height + 15) / 16, 1)
        GLES32.glMemoryBarrier(GLES32.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT)

        flip = 1 - flip

        GLES32.glViewport(0, 0, width, height)
        GLES32.glUseProgram(compositeProgram)

        GLES32.glActiveTexture(GLES32.GL_TEXTURE0)
        GLES32.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, uiTexId)
        GLES32.glUniform1i(GLES32.glGetUniformLocation(compositeProgram, "uUi"), 0)

        GLES32.glActiveTexture(GLES32.GL_TEXTURE1)
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, filmOut)
        GLES32.glUniform1i(GLES32.glGetUniformLocation(compositeProgram, "uFilm"), 1)

        GLES32.glUniform2f(GLES32.glGetUniformLocation(compositeProgram, "uRes"), width.toFloat(), height.toFloat())
        GLES32.glUniform1f(GLES32.glGetUniformLocation(compositeProgram, "uTime"), elapsed)
        GLES32.glUniformMatrix4fv(
            GLES32.glGetUniformLocation(compositeProgram, "uStMat"), 1, false,
            if (matrixReady) stMatrix else IDENT, 0
        )

        GLES32.glBindVertexArray(vao)
        GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, 3)
    }

    private fun initDrops() {
        val data = FloatArray(DROPS * 8)
        for (i in 0 until DROPS) {
            val b = i * 8
            data[b] = Random.nextFloat() * width
            data[b + 1] = Random.nextFloat() * height
            val r = when {
                i % 9 == 0 -> 26f + Random.nextFloat() * 16f
                i % 3 == 0 -> 14f + Random.nextFloat() * 10f
                else -> 7f + Random.nextFloat() * 6f
            }
            data[b + 2] = 0f
            data[b + 3] = r
            data[b + 4] = 0f
            data[b + 5] = 0f
            data[b + 6] = 0f
            data[b + 7] = 1f
        }
        val buf: FloatBuffer = ByteBuffer.allocateDirect(data.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().put(data)
        buf.position(0)

        val bs = IntArray(1)
        GLES32.glGenBuffers(1, bs, 0)
        dropSsbo = bs[0]
        GLES32.glBindBuffer(GLES32.GL_SHADER_STORAGE_BUFFER, dropSsbo)
        GLES32.glBufferData(GLES32.GL_SHADER_STORAGE_BUFFER, data.size * 4, buf, GLES32.GL_DYNAMIC_DRAW)
    }

    private fun initFilmTextures() {
        val texs = intArrayOf(filmA, filmB)
        if (filmA != 0) GLES32.glDeleteTextures(2, texs, 0)
        GLES32.glGenTextures(2, texs, 0)
        filmA = texs[0]
        filmB = texs[1]
        for (t in texs) {
            GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, t)
            GLES32.glTexStorage2D(GLES32.GL_TEXTURE_2D, 1, GLES32.GL_RGBA16F, width, height)
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR)
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR)
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_CLAMP_TO_EDGE)
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_CLAMP_TO_EDGE)
            GLES31.glClearTexImage(t, 0, GLES32.GL_RGBA, GLES32.GL_HALF_FLOAT, null)
        }
        GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER, 0)
    }

    private fun buildCompute(src: String, tag: String): Int {
        val sh = GLES32.glCreateShader(GLES32.GL_COMPUTE_SHADER)
        GLES32.glShaderSource(sh, src)
        GLES32.glCompileShader(sh)
        val ok = IntArray(1)
        GLES32.glGetShaderiv(sh, GLES32.GL_COMPILE_STATUS, ok, 0)
        if (ok[0] == 0) {
            Log.e(TAG, "$tag compile: ${GLES32.glGetShaderInfoLog(sh)}")
            GLES32.glDeleteShader(sh)
            return 0
        }
        val p = GLES32.glCreateProgram()
        GLES32.glAttachShader(p, sh)
        GLES32.glLinkProgram(p)
        GLES32.glGetProgramiv(p, GLES32.GL_LINK_STATUS, ok, 0)
        if (ok[0] == 0) {
            Log.e(TAG, "$tag link: ${GLES32.glGetProgramInfoLog(p)}")
            return 0
        }
        GLES32.glDeleteShader(sh)
        return p
    }

    private fun buildRender(vsSrc: String, fsSrc: String, tag: String): Int {
        fun sh(type: Int, src: String): Int {
            val s = GLES32.glCreateShader(type)
            GLES32.glShaderSource(s, src)
            GLES32.glCompileShader(s)
            val ok = IntArray(1)
            GLES32.glGetShaderiv(s, GLES32.GL_COMPILE_STATUS, ok, 0)
            if (ok[0] == 0) {
                Log.e(TAG, "$tag shader: ${GLES32.glGetShaderInfoLog(s)}")
                return 0
            }
            return s
        }
        val vs = sh(GLES32.GL_VERTEX_SHADER, vsSrc)
        val fs = sh(GLES32.GL_FRAGMENT_SHADER, fsSrc)
        if (vs == 0 || fs == 0) return 0
        val p = GLES32.glCreateProgram()
        GLES32.glAttachShader(p, vs)
        GLES32.glAttachShader(p, fs)
        GLES32.glLinkProgram(p)
        val ok = IntArray(1)
        GLES32.glGetProgramiv(p, GLES32.GL_LINK_STATUS, ok, 0)
        if (ok[0] == 0) {
            Log.e(TAG, "$tag link: ${GLES32.glGetProgramInfoLog(p)}")
            return 0
        }
        GLES32.glDeleteShader(vs)
        GLES32.glDeleteShader(fs)
        return p
    }

    private fun u1(p: Int, n: String, v: Float) {
        GLES32.glUniform1f(GLES32.glGetUniformLocation(p, n), v)
    }

    private fun u2(p: Int, n: String, a: Float, b: Float) {
        GLES32.glUniform2f(GLES32.glGetUniformLocation(p, n), a, b)
    }


    private val IDENT = floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f
    )
}

private const val PHYSICS_CS = """#version 320 es
layout(local_size_x = 64) in;

struct D { vec4 p; vec4 v; };
layout(std430, binding = 0) buffer B { D d[]; };
layout(rgba16f, binding = 1) uniform image2D film;

uniform vec2 uSim;
uniform float uDt;
uniform float uTime;

const float GRAV = 9.81;
const float SIGMA = 0.0728;
const float MU = 0.001;

uint rngState;

float rnd() {
    rngState = rngState * 747796405u + 2891336453u;
    uint w = ((rngState >> ((rngState >> 28u) + 4u)) ^ rngState) * 277803737u;
    return float((w >> 22u) ^ w) / 4294967296.0;
}

void main() {
    uint i = gl_GlobalInvocationID.x;
    if (i >= d.length()) return;
    rngState = uint(i) * 9781u + uint(uTime * 60.0);

    D drop = d[i];
    if (drop.v.w < 0.5) return;

    float rPx = drop.p.w;
    float rM = rPx / 15000.0;
    float vol = 1.35 * rM * rM * rM;
    float mass = 998.0 * vol;

    float drive = mass * GRAV;
    float pinBase = 3.14159 * 2.0 * rM * SIGMA * 0.49;
    float defect = 1.0 + 0.32 * sin(drop.p.x * 0.013) * cos(drop.p.y * 0.011);
    float pin = pinBase * defect;

    if (drive > pin) {
        float net = (drive - pin * 0.88);
        float drag = 3.6 * MU * rM * 12.0 * drop.v.y + net * 0.24;
        drop.v.y = clamp(drop.v.y + (net / max(mass, 1e-12)) * uDt * 0.55, 0.5, 22.0);
        drop.v.x = clamp(drop.v.x + sin(drop.p.x * 0.02 + uTime * 0.7) * 4.0 * uDt, -4.0, 4.0);
    } else {
        drop.v.y *= 0.86;
    }

    drop.p.y += drop.v.y * uDt;
    drop.p.x += drop.v.x * uDt;

    if (drop.v.y > 0.5) {
        float lose = (0.35 + drop.v.y * 0.10) * uDt;
        rPx = max(4.0, rPx - lose);
    }
    drop.p.w = rPx;

    if (drop.p.y - rPx > uSim.y) {
        drop.p.x = rnd() * uSim.x;
        drop.p.y = -20.0 - rnd() * 80.0;
        rPx = mix(14.0, 42.0, rnd());
        drop.p.w = rPx;
        drop.v.y = 0.0;
        drop.v.x = 0.0;
    }

    int rr = int(ceil(rPx));
    ivec2 c = ivec2(drop.p.xy);
    for (int oy = -rr; oy <= rr; oy += 1) {
        for (int ox = -rr; ox <= rr; ox += 1) {
            ivec2 q = c + ivec2(ox, oy);
            if (q.x < 0 || q.y < 0 || q.x >= int(uSim.x) || q.y >= int(uSim.y)) continue;
            vec2 e = vec2(float(ox), float(oy)) / rPx;
            float r2 = dot(e, e);
            if (r2 < 1.0) {
                float cap = sqrt(1.0 - r2);
                vec4 cur = imageLoad(film, q);
                cur.r = max(cur.r, cap);
                cur.g = 1.0;
                imageStore(film, q, cur);
            }
        }
    }

    d[i] = drop;
}
"""

private const val FILM_PDE_CS = """#version 320 es
layout(local_size_x = 16, local_size_y = 16) in;

layout(rgba16f, binding = 0) uniform readonly image2D fin;
layout(rgba16f, binding = 1) uniform image2D fout;

uniform float uDt;

void main() {
    ivec2 p = ivec2(gl_GlobalInvocationID.xy);
    ivec2 sz = imageSize(fin);
    if (p.x >= sz.x || p.y >= sz.y) return;

    vec4 c = imageLoad(fin, p);
    vec4 l = imageLoad(fin, clamp(p - ivec2(1, 0), ivec2(0), sz - 1));
    vec4 r = imageLoad(fin, clamp(p + ivec2(1, 0), ivec2(0), sz - 1));
    vec4 u = imageLoad(fin, clamp(p - ivec2(0, 1), ivec2(0), sz - 1));
    vec4 dn = imageLoad(fin, clamp(p + ivec2(0, 1), ivec2(0), sz - 1));

    float lap = (l.r + r.r + u.r + dn.r) * 0.25 - c.r;
    float vel = c.b + lap * 14.0 * uDt;
    vel *= exp(-2.8 * uDt);
    float h = max(0.0, c.r + vel * uDt - 0.030 * uDt);

    imageStore(fout, p, vec4(h, c.g * exp(-0.040 * uDt), vel, 1.0));
}
"""

private const val FULLSCREEN_VS = """#version 320 es
out vec2 vPx;
uniform vec2 uRes;
void main() {
    vec2 pos = vec2(float((gl_VertexID << 1) & 2), float(gl_VertexID & 2));
    vPx = pos * uRes;
    gl_Position = vec4(pos * 2.0 - 1.0, 0.0, 1.0);
}
"""

private const val COMPOSITE_FS = """#version 320 es
#extension GL_OES_EGL_image_external_essl3 : require
precision highp float;

in vec2 vPx;
out vec4 outColor;

uniform samplerExternalOES uUi;
uniform sampler2D uFilm;
uniform mat4 uStMat;
uniform vec2 uRes;
uniform float uTime;

vec2 uiUv(vec2 px) {
    vec2 uv = px / uRes;
    return clamp((uStMat * vec4(uv, 0.0, 1.0)).xy, vec2(0.001), vec2(0.999));
}

float h12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

vec2 h22(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * vec3(0.1031, 0.1030, 0.0973));
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.xx + p3.yz) * p3.zy);
}

float n2(vec2 p) {
    vec2 i = floor(p), f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(h12(i), h12(i + vec2(1, 0)), u.x), mix(h12(i + vec2(0, 1)), h12(i + vec2(1, 1)), u.x), u.y);
}

float fbm(vec2 p) {
    float v = 0.0, a = 0.5;
    for (int i = 0; i < 4; i++) { v += a * n2(p); p = p * 2.03 + vec2(13.7, 7.3); a *= 0.5; }
    return v;
}

vec3 env(vec3 R) {
    float up = clamp(-R.y * 0.5 + 0.5, 0.0, 1.0);
    vec3 sky = mix(vec3(0.22, 0.26, 0.33), vec3(0.76, 0.83, 0.90), smoothstep(0.20, 0.55, up));
    if (R.y < 0.06) {
        float c = fbm(R.xz / max(0.2, -R.y + 0.24) * 1.3 + uTime * 0.02);
        sky = mix(sky, mix(vec3(0.50, 0.56, 0.64), vec3(0.85, 0.90, 0.95), smoothstep(0.35, 0.75, c)), 0.6);
    }
    return sky;
}

void main() {
    vec4 filmC = texture(uFilm, vPx / uRes);
    float h = filmC.r;
    float wet = filmC.g;

    vec3 base = texture(uUi, uiUv(vPx)).rgb;
    base *= vec3(0.988, 0.997, 1.016);

    vec3 col = base;

    if (h > 0.012) {
        vec2 t = 2.0 / uRes;
        float hL = texture(uFilm, vPx / uRes - vec2(t.x, 0.0)).r;
        float hR = texture(uFilm, vPx / uRes + vec2(t.x, 0.0)).r;
        float hD = texture(uFilm, vPx / uRes - vec2(0.0, t.y)).r;
        float hU = texture(uFilm, vPx / uRes + vec2(0.0, t.y)).r;

        vec3 N = normalize(vec3((hL - hR) * 3.2, (hU - hD) * 3.2, 1.0));

        float bend = h * 22.0;
        vec2 dir = -N.xy * bend;
        vec3 refr = texture(uUi, uiUv(vPx + dir)).rgb;

        float edge = smoothstep(0.012, 0.05, h) * (1.0 - smoothstep(0.12, 0.32, h));
        refr *= mix(1.0, 1.35, edge * 0.6 + h * 0.5);

        float fres = 0.02 + 0.98 * pow(1.0 - clamp(N.z, 0.0, 1.0), 4.0);
        vec3 skyRef = env(reflect(vec3(0.0, 0.0, -1.0), N));
        vec3 wetCol = mix(refr, skyRef, clamp(fres * 0.65, 0.0, 0.55));

        float hl = pow(max(dot(N, normalize(vec3(-0.55, 0.62, 0.56))), 0.0), 60.0);
        wetCol += vec3(1.0, 0.99, 0.97) * hl * 1.3;

        float caustic = pow(clamp(h * 2.6, 0.0, 1.0), 3.0) * (1.0 - smoothstep(0.5, 0.95, h));
        wetCol += vec3(0.9, 0.97, 1.0) * caustic * 0.28;

        col = mix(col, wetCol, smoothstep(0.012, 0.06, h));
    }

    if (wet > 0.02 && h < 0.012) {
        float trailGlow = wet * (1.0 - smoothstep(0.0, 0.012, h));
        col += vec3(0.05, 0.07, 0.09) * trailGlow;
        col = mix(col, col * 1.14 + vec3(0.02, 0.03, 0.04), trailGlow * 0.4);
    }

    for (int k = 0; k < 5; k++) {
        float fk = float(k);
        float sp = 1.4 + h12(vec2(fk * 7.7, 3.1)) * 1.8;
        float cyc = fract(uTime * sp * 0.4 + h12(vec2(fk * 3.3, 9.1)));
        float xp = h12(vec2(floor(uTime * sp * 0.4 + h12(vec2(fk, 2.2))), fk * 11.3)) * uRes.x;
        float tip = cyc * (uRes.y + 160.0);
        float len = 70.0 + h12(vec2(fk, 5.5)) * 90.0;
        float dx = abs(vPx.x - xp);
        if (dx < 1.6) {
            float dy = tip - (uRes.y - vPx.y);
            if (dy > 0.0 && dy < len) {
                float m = smoothstep(0.0, 0.2, dy / len) * (1.0 - smoothstep(0.7, 1.0, dy / len));
                col += vec3(0.10, 0.12, 0.14) * m * smoothstep(1.6, 0.2, dx);
            }
        }
    }

    outColor = vec4(col, 1.0);
}
"""