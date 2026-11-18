package com.cmc.launcher.ui.screens.content.elements

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
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
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.max
import kotlin.random.Random

@SuppressLint("ViewConstructor")
class RainGLES32OverlayLayout(
    context: Context,
    contentView: View
) : FrameLayout(context) {

    private val glView = GLSurfaceView(context).apply {
        setEGLContextClientVersion(3)
        setZOrderOnTop(true)
        isClickable = false
        isFocusable = false
    }

    private val renderer = RainGLES32Renderer(context)

    private var uiSurfaceTexture: SurfaceTexture? = null
    private var uiSurface: Surface? = null
    private val capturedView: View = contentView

    private val captureCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (renderer.blackStreak < 2 && glView.visibility == VISIBLE) {
                captureUiToSurface()
            }
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    init {
        glView.setRenderer(renderer)
        glView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        addView(contentView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(glView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        post(object : Runnable {
            override fun run() {
                if (renderer.blackStreak >= 2) {
                    glView.visibility = GONE
                    Log.w(TAG, "black output detected, GL layer hidden")
                } else {
                    postDelayed(this, 500)
                }
            }
        })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Choreographer.getInstance().postFrameCallback(captureCallback)
    }

    override fun onDetachedFromWindow() {
        Choreographer.getInstance().removeFrameCallback(captureCallback)
        uiSurface?.release()
        uiSurface = null
        super.onDetachedFromWindow()
    }

    private fun captureUiToSurface() {
        val w = capturedView.width
        val h = capturedView.height
        if (w <= 0 || h <= 0) return
        val texId = renderer.uiExternalTexId
        if (texId == 0) return

        if (uiSurfaceTexture == null) {
            try {
                uiSurfaceTexture = SurfaceTexture(texId).apply {
                    setDefaultBufferSize(w, h)
                    uiSurface = Surface(this)
                    renderer.bindSurfaceTexture(this)
                }
            } catch (e: Throwable) {
                Log.e(TAG, "SurfaceTexture fail", e)
                return
            }
        }

        val surface = uiSurface ?: return
        if (!surface.isValid) return
        try {
            val canvas = surface.lockHardwareCanvas()
            capturedView.draw(canvas)
            surface.unlockCanvasAndPost(canvas)
        } catch (_: Throwable) {
        }
    }

    companion object {
        private const val TAG = "RainGL"
    }
}

private class RainGLES32Renderer(private val context: Context) : GLSurfaceView.Renderer {

    companion object {
        private const val TAG = "RainGL"
        private const val MAX_DROPS = 512
    }

    private val pxPerMeter = (context.resources.displayMetrics.xdpi / 0.0254f).coerceAtLeast(12000f)

    @Volatile
    var uiExternalTexId = 0
        private set

    @Volatile
    var framesDrawn = 0
        private set

    @Volatile
    var blackStreak = 0
        private set

    @Volatile
    private var surfaceTexture: SurfaceTexture? = null

    private val stMatrix = FloatArray(16)
    private var matrixReady = false

    private var width = 720
    private var height = 1610

    private var dropPhysicsProgram = 0
    private var heightFieldProgram = 0
    private var clearSplatProgram = 0
    private var clearCausticsProgram = 0
    private var causticPhotonProgram = 0
    private var renderProgram = 0

    private var dropSsbo = 0
    private var heightTexA = 0
    private var heightTexB = 0
    private var splatTex = 0
    private var causticIntTex = 0

    private var quadVao = 0
    private var lastNanos = System.nanoTime()
    private var elapsedSec = 0f

    fun bindSurfaceTexture(st: SurfaceTexture) {
        surfaceTexture = st
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val tex = IntArray(1)
        GLES32.glGenTextures(1, tex, 0)
        uiExternalTexId = tex[0]
        GLES32.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, uiExternalTexId)
        GLES32.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR)
        GLES32.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR)
        GLES32.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_CLAMP_TO_EDGE)
        GLES32.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_CLAMP_TO_EDGE)

        dropPhysicsProgram = compileCompute(DROP_PHYSICS_CS, "dropPhysics")
        heightFieldProgram = compileCompute(HEIGHTFIELD_PDE_CS, "heightField")
        clearSplatProgram = compileCompute(CLEAR_SPLAT_CS, "clearSplat")
        clearCausticsProgram = compileCompute(CLEAR_CAUSTICS_CS, "clearCaustics")
        causticPhotonProgram = compileCompute(CAUSTICS_PHOTON_CS, "causticPhoton")
        renderProgram = compileRender(FULLSCREEN_VS, FINAL_OPTICS_FS, "finalOptics")

        val vaos = IntArray(1)
        GLES32.glGenVertexArrays(1, vaos, 0)
        quadVao = vaos[0]

        initDropSsbo()
        Log.i(TAG, "GL pipeline ready")
    }

    override fun onSurfaceChanged(gl: GL10?, w: Int, h: Int) {
        width = max(1, w)
        height = max(1, h)
        surfaceTexture?.setDefaultBufferSize(width, height)
        initSimulationTextures()
        Log.i(TAG, "surface ${width}x${height}")
    }

    override fun onDrawFrame(gl: GL10?) {
        val now = System.nanoTime()
        val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.033f)
        lastNanos = now
        elapsedSec += dt

        surfaceTexture?.let {
            try {
                it.updateTexImage()
                it.getTransformMatrix(stMatrix)
                matrixReady = true
            } catch (_: Throwable) {
            }
        }

        val inTex = if (pingPong == 0) heightTexA else heightTexB
        val outTex = if (pingPong == 0) heightTexB else heightTexA

        // 1. 清空 splat R32I
        GLES32.glUseProgram(clearSplatProgram)
        GLES32.glBindImageTexture(0, splatTex, 0, false, 0, GLES32.GL_WRITE_ONLY, GLES32.GL_R32I)
        GLES32.glDispatchCompute((width + 15) / 16, (height + 15) / 16, 1)
        GLES32.glMemoryBarrier(GLES32.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT)

        // 2. DROP_PHYSICS_CS: 512 颗 SSBO 物理水滴，球冠 splat 写 R32I
        GLES32.glUseProgram(dropPhysicsProgram)
        GLES32.glBindBufferBase(GLES32.GL_SHADER_STORAGE_BUFFER, 0, dropSsbo)
        GLES32.glUniform2f(loc(dropPhysicsProgram, "uSimSize"), width.toFloat(), height.toFloat())
        GLES32.glUniform1f(loc(dropPhysicsProgram, "uPxPerMeter"), pxPerMeter)
        GLES32.glUniform1f(loc(dropPhysicsProgram, "uDt"), dt)
        GLES32.glUniform1f(loc(dropPhysicsProgram, "uTime"), elapsedSec)
        GLES32.glBindImageTexture(2, splatTex, 0, false, 0, GLES32.GL_READ_WRITE, GLES32.GL_R32I)
        GLES32.glDispatchCompute(MAX_DROPS / 64, 1, 1)
        GLES32.glMemoryBarrier(GLES32.GL_SHADER_STORAGE_BARRIER_BIT or GLES32.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT)

        // 3. HEIGHTFIELD_PDE_CS: in+splat readonly → out writeonly
        GLES32.glUseProgram(heightFieldProgram)
        GLES32.glBindImageTexture(0, inTex, 0, false, 0, GLES32.GL_READ_ONLY, GLES32.GL_RGBA16F)
        GLES32.glBindImageTexture(1, splatTex, 0, false, 0, GLES32.GL_READ_ONLY, GLES32.GL_R32F)
        GLES32.glBindImageTexture(2, outTex, 0, false, 0, GLES32.GL_WRITE_ONLY, GLES32.GL_RGBA16F)
        GLES32.glUniform1f(loc(heightFieldProgram, "uDt"), dt)
        GLES32.glDispatchCompute((width + 15) / 16, (height + 15) / 16, 1)
        GLES32.glMemoryBarrier(GLES32.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT)

        // 3. 清空焦散 R32I
        GLES32.glUseProgram(clearCausticsProgram)
        GLES32.glBindImageTexture(0, causticIntTex, 0, false, 0, GLES32.GL_WRITE_ONLY, GLES32.GL_R32I)
        GLES32.glDispatchCompute((width + 15) / 16, (height + 15) / 16, 1)
        GLES32.glMemoryBarrier(GLES32.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT)

        // 4. CAUSTICS_PHOTON_CS: RGB 色散光子投射
        GLES32.glUseProgram(causticPhotonProgram)
        GLES32.glBindImageTexture(0, outTex, 0, false, 0, GLES32.GL_READ_ONLY, GLES32.GL_RGBA16F)
        GLES32.glBindImageTexture(1, causticIntTex, 0, false, 0, GLES32.GL_READ_WRITE, GLES32.GL_R32I)
        GLES32.glDispatchCompute((width + 15) / 16, (height + 15) / 16, 1)
        GLES32.glMemoryBarrier(GLES32.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT or GLES32.GL_TEXTURE_FETCH_BARRIER_BIT)

        pingPong = 1 - pingPong
        GLES32.glViewport(0, 0, width, height)
        GLES32.glUseProgram(renderProgram)

        GLES32.glActiveTexture(GLES32.GL_TEXTURE0)
        GLES32.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, uiExternalTexId)
        GLES32.glUniform1i(loc(renderProgram, "uUiTex"), 0)
        GLES32.glActiveTexture(GLES32.GL_TEXTURE1)
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, outTex)
        GLES32.glUniform1i(loc(renderProgram, "uHeightTex"), 1)
        GLES32.glBindImageTexture(2, causticIntTex, 0, false, 0, GLES32.GL_READ_ONLY, GLES32.GL_R32I)
        GLES32.glActiveTexture(GLES32.GL_TEXTURE3)
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, splatTex)
        GLES32.glUniform1i(loc(renderProgram, "uSplatTex"), 3)
        GLES32.glBindBufferBase(GLES32.GL_SHADER_STORAGE_BUFFER, 3, dropSsbo)
        GLES32.glUniform2f(loc(renderProgram, "uResolution"), width.toFloat(), height.toFloat())
        GLES32.glUniform1f(loc(renderProgram, "uGlassThicknessPx"), 0.004f * pxPerMeter * 0.25f)
        GLES32.glUniform1f(loc(renderProgram, "uUiPlaneZ"), 0.0011f * pxPerMeter)
        GLES32.glUniform1f(loc(renderProgram, "uTime"), elapsedSec)
        GLES32.glUniformMatrix4fv(
            loc(renderProgram, "uStMatrix"), 1, false,
            if (matrixReady) stMatrix else IDENT, 0
        )

        GLES32.glBindVertexArray(quadVao)
        GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, 3)
        framesDrawn++
        if (framesDrawn == 30 || framesDrawn == 150) {
            val sw = 16
            val sh = 16
            val px = IntBuffer.allocate(sw * sh)
            GLES32.glReadPixels(width / 2 - sw / 2, height / 2 - sh / 2, sw, sh, GLES32.GL_RGBA, GLES32.GL_UNSIGNED_BYTE, px)
            var sum = 0L
            var mx = 0
            for (i in 0 until sw * sh) {
                val v = px.get(i)
                val l = ((v and 0xFF) + ((v shr 8) and 0xFF) + ((v shr 16) and 0xFF)) / 3
                sum += l
                if (l > mx) mx = l
            }
            val avg = (sum / (sw * sh)).toInt()
            if (avg < 4 && mx < 12) blackStreak++
            Log.i(TAG, "frame=$framesDrawn avg=$avg max=$mx matrixReady=$matrixReady blackStreak=$blackStreak")
            try {
                val diag = java.io.File(context.getExternalFilesDir(null), "rain_diag.txt")
                diag.writeText(
                    "frame=$framesDrawn" + "\n" +
                    "avg=$avg" + "\n" +
                    "max=$mx" + "\n" +
                    "matrixReady=$matrixReady" + "\n" +
                    "blackStreak=$blackStreak" + "\n" +
                    "size=${width}x$height" + "\n"
                )
            } catch (_: Throwable) {
            }
        }
    }

    private var pingPong = 0

    private fun loc(p: Int, n: String) = GLES32.glGetUniformLocation(p, n)

    private val IDENT = floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f
    )

    private fun initDropSsbo() {
        val data = FloatArray(MAX_DROPS * 8)
        for (i in 0 until MAX_DROPS) {
            val base = i * 8
            if (i >= 384) {
                data[base] = 0f
                data[base + 1] = 0f
                data[base + 2] = 0f
                data[base + 3] = 0f
                data[base + 4] = 0f
                data[base + 5] = 0f
                data[base + 6] = 0f
                data[base + 7] = 0f
                continue
            }
            val u = Random.nextFloat().coerceIn(1.0e-4f, 0.9999f)
            val dM = ((-kotlin.math.ln(1.0 - u.toDouble())) / 2.6e-3).toFloat().coerceIn(1.6e-4f, 4.0e-3f)
            val rM = 0.5f * dM
            val landed = i % 3 == 0
            data[base] = Random.nextFloat() * width
            data[base + 1] = if (landed) Random.nextFloat() * height else -20f - Random.nextFloat() * 1500f
            data[base + 2] = 0.5236f * dM * dM * dM
            data[base + 3] = rM * pxPerMeter
            data[base + 4] = 0f
            data[base + 5] = 0f
            data[base + 6] = if (landed) 1f else 0f
            data[base + 7] = 1f
        }
        val buf: FloatBuffer = ByteBuffer.allocateDirect(data.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(data)
        buf.position(0)

        val buffers = IntArray(1)
        GLES32.glGenBuffers(1, buffers, 0)
        dropSsbo = buffers[0]
        GLES32.glBindBuffer(GLES32.GL_SHADER_STORAGE_BUFFER, dropSsbo)
        GLES32.glBufferData(GLES32.GL_SHADER_STORAGE_BUFFER, data.size * 4, buf, GLES32.GL_DYNAMIC_DRAW)
    }

    private fun initSimulationTextures() {
        val texs = intArrayOf(heightTexA, heightTexB, splatTex, causticIntTex)
        if (heightTexA != 0) GLES32.glDeleteTextures(4, texs, 0)
        GLES32.glGenTextures(4, texs, 0)
        heightTexA = texs[0]
        heightTexB = texs[1]
        splatTex = texs[2]
        causticIntTex = texs[3]

        val fbos = IntArray(1)
        GLES32.glGenFramebuffers(1, fbos, 0)
        GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER, fbos[0])

        for (i in 0..1) {
            GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, texs[i])
            GLES32.glTexStorage2D(GLES32.GL_TEXTURE_2D, 1, GLES32.GL_RGBA16F, width, height)
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR)
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR)
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_CLAMP_TO_EDGE)
            GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_CLAMP_TO_EDGE)
            GLES32.glFramebufferTexture2D(GLES32.GL_FRAMEBUFFER, GLES32.GL_COLOR_ATTACHMENT0, GLES32.GL_TEXTURE_2D, texs[i], 0)
            GLES32.glClearColor(0f, 0f, 0f, 0f)
            GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT)
        }

        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, splatTex)
        GLES32.glTexStorage2D(GLES32.GL_TEXTURE_2D, 1, GLES32.GL_R32I, width, height)
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_NEAREST)
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_NEAREST)
        GLES32.glFramebufferTexture2D(GLES32.GL_FRAMEBUFFER, GLES32.GL_COLOR_ATTACHMENT0, GLES32.GL_TEXTURE_2D, splatTex, 0)
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT)

        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, causticIntTex)
        GLES32.glTexStorage2D(GLES32.GL_TEXTURE_2D, 1, GLES32.GL_R32I, width, height)
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_NEAREST)
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_NEAREST)

        GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER, 0)
        GLES32.glDeleteFramebuffers(1, fbos, 0)
    }

    private fun compileCompute(src: String, tag: String): Int {
        val sh = GLES32.glCreateShader(GLES32.GL_COMPUTE_SHADER)
        GLES32.glShaderSource(sh, src)
        GLES32.glCompileShader(sh)
        check(sh, tag)
        val prog = GLES32.glCreateProgram()
        GLES32.glAttachShader(prog, sh)
        GLES32.glLinkProgram(prog)
        checkLink(prog, tag)
        GLES32.glDeleteShader(sh)
        return prog
    }

    private fun compileRender(vsSrc: String, fsSrc: String, tag: String): Int {
        val vs = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER)
        GLES32.glShaderSource(vs, vsSrc)
        GLES32.glCompileShader(vs)
        check(vs, tag + ":vs")
        val fs = GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER)
        GLES32.glShaderSource(fs, fsSrc)
        GLES32.glCompileShader(fs)
        check(fs, tag + ":fs")
        val prog = GLES32.glCreateProgram()
        GLES32.glAttachShader(prog, vs)
        GLES32.glAttachShader(prog, fs)
        GLES32.glLinkProgram(prog)
        checkLink(prog, tag)
        GLES32.glDeleteShader(vs)
        GLES32.glDeleteShader(fs)
        return prog
    }

    private fun check(shader: Int, tag: String) {
        val ok = IntArray(1)
        GLES32.glGetShaderiv(shader, GLES32.GL_COMPILE_STATUS, ok, 0)
        if (ok[0] == 0) {
            val info = GLES32.glGetShaderInfoLog(shader)
            Log.e(TAG, "$tag compile FAILED: $info")
            try {
                java.io.File(context.getExternalFilesDir(null), "shader_err.txt")
                    .writeText("$tag FAILED:\n$info\n\n")
            } catch (_: Throwable) {
            }
        }
    }

    private fun checkLink(prog: Int, tag: String) {
        val ok = IntArray(1)
        GLES32.glGetProgramiv(prog, GLES32.GL_LINK_STATUS, ok, 0)
        if (ok[0] == 0) {
            Log.e(TAG, "$tag link FAILED: ${GLES32.glGetProgramInfoLog(prog)}")
        }
    }
}

private const val DROP_PHYSICS_CS = """#version 320 es
precision highp float;
precision highp int;
precision highp image2D;
layout(local_size_x = 64) in;
struct Drop {
    vec4 posVol;
    vec4 velState;
};
layout(std430, binding = 0) buffer DropBuffer {
    Drop drops[];
};
layout(r32i, binding = 2) uniform highp iimage2D splat;
uniform vec2 uSimSize;
uniform float uPxPerMeter;
uniform float uDt;
uniform float uTime;

const float RHO_W = 998.2;
const float RHO_AIR = 1.204;
const float SIGMA = 0.0728;
const float MU_W = 1.002e-3;
const float MU_AIR = 1.825e-5;
const float G_ACC = 9.81;
const float LAMBDA_SLIP = 1.0e-8;

const float THETA_EQ = 1.0471976;
const float THETA_ADV = 1.3613568;
const float THETA_REC = 0.7330383;
const float R_WENZEL = 1.35;
const float F_SOLID = 0.86;

const float D_VAPOR = 2.62e-5;
const float C_SAT = 0.017290;
const float C_INF = 0.010374;
const float EVAP_VENT = 0.15;
const float D_GAMMA_DT = 1.5e-4;

const float MP_LAMBDA = 3.6;
const float D_MIN_M = 1.6e-4;
const float D_MAX_M = 4.0e-3;

const float PI_F = 3.14159265;

float h21(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float vnoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(h21(i), h21(i + vec2(1.0, 0.0)), u.x), mix(h21(i + vec2(0.0, 1.0)), h21(i + vec2(1.0, 1.0)), u.x), u.y);
}

float dustField(vec2 pPx) {
    float n1 = vnoise(pPx * 0.05);
    float n2 = vnoise(pPx * 0.017);
    float n3 = vnoise(pPx * 0.27);
    return clamp(0.45 * n1 + 0.35 * n2 + 0.20 * n3, 0.0, 1.0);
}

float mpDiameter(float u) {
    float d = -(log(max(1.0e-6, 1.0 - u))) / MP_LAMBDA * 1.0e-3;
    return clamp(d, D_MIN_M, D_MAX_M);
}

float terminalVel(float dM) {
    float dMm = dM * 1000.0;
    if (dMm < 1.0) {
        return G_ACC * dM * dM * (RHO_W - RHO_AIR) / (18.0 * MU_AIR);
    }
    float cd = mix(1.10, 0.55, smoothstep(1.0, 4.0, dMm));
    float v = sqrt(4.0 * RHO_W * G_ACC * dM / (3.0 * cd * RHO_AIR));
    return min(v, 9.2);
}

float contactCosCb() {
    float c = cos(THETA_EQ);
    return clamp(R_WENZEL * F_SOLID * c - (1.0 - F_SOLID), -0.99, 0.99);
}

float furmidgePin(float aM, float dust) {
    float cA = cos(THETA_ADV);
    float cR = cos(THETA_REC);
    float hysteresis = cR - cA;
    float rough = mix(1.0, R_WENZEL, 0.55);
    float f = 2.0 * aM * SIGMA * hysteresis * rough;
    return f * (0.62 + 0.95 * dust);
}

float huhScriven(float aM, float vM, float thRad) {
    float lnT = log(max(2.0, aM / LAMBDA_SLIP));
    return 3.0 * MU_W * aM * abs(vM) * lnT / max(0.16, thRad);
}

float tannerVel(float aM, float thRad) {
    float thEq = THETA_EQ;
    float d2 = max(0.0, thRad * thRad - thEq * thEq);
    float v = SIGMA * thRad * d2 / (3.0 * MU_W * max(2.0e-3, 2.0 * aM));
    return clamp(v, 0.0, 0.35);
}

float thRadGlobal() {
    float c = cos(THETA_EQ);
    float cb = clamp(R_WENZEL * F_SOLID * c - (1.0 - F_SOLID), -0.99, 0.99);
    return acos(clamp(cb, -0.99, 0.99));
}

float stefanRate(float dM) {
    float dc = C_SAT - C_INF;
    return 2.0 * D_VAPOR * dc * EVAP_VENT / (RHO_W * max(1.0e-4, dM));
}

void main() {
    uint idx = gl_GlobalInvocationID.x;
    if (idx >= uint(drops.length())) return;
    bool isSat = idx >= 384u;
    Drop d = drops[idx];
    if (d.velState.w < 0.5) return;

    if (isSat) {
        float age = d.velState.z + uDt;
        float aSat = d.posVol.w / uPxPerMeter;
        float vSat = length(d.velState.xy);
        float dragAcc = -6.0 * PI_F * MU_AIR * aSat * vSat / max(1.0e-12, RHO_W * 2.09 * aSat * aSat * aSat);
        d.velState.y += (G_ACC + dragAcc * d.velState.y) * uDt;
        d.velState.x += dragAcc * d.velState.x * 0.35 * uDt;
        d.posVol.xy += d.velState.xy * uPxPerMeter * uDt;
        if (d.posVol.y < -40.0 || d.posVol.y > uSimSize.y + 40.0 || d.posVol.x < -40.0 || d.posVol.x > uSimSize.x + 40.0) {
            d.velState.w = 0.0;
        }
        d.velState.z = age;
        if (age > mix(0.5, 1.4, h21(vec2(float(idx), 3.1)))) {
            d.velState.w = 0.0;
        }
        if (d.velState.w > 0.5) {
            float aPxS = d.posVol.w;
            ivec2 cS = ivec2(d.posVol.xy);
            int radS = int(clamp(aPxS * 1.05, 1.0, 20.0));
            float dep = clamp(0.20 * exp(-age * 2.2), 0.0, 0.20);
            for (int oy = -radS; oy <= radS; oy++) {
                for (int ox = -radS; ox <= radS; ox++) {
                    ivec2 pS = cS + ivec2(ox, oy);
                    if (pS.x < 0 || pS.y < 0 || pS.x >= int(uSimSize.x) || pS.y >= int(uSimSize.y)) continue;
                    vec2 qS = vec2(float(ox), float(oy)) / max(1.0, aPxS);
                    float r2S = dot(qS, qS);
                    if (r2S < 1.0) {
                        int depthI = int(clamp(sqrt(1.0 - r2S) * dep * 1023.0, 1.0, 1023.0));
                        int packedS = (depthI << 12) | int(idx & 0xFFFu);
                        imageAtomicMax(splat, pS, packedS);
                    }
                }
            }
        }
        drops[idx] = d;
        return;
    }

    if (d.velState.z < 0.5) {
        float seed = fract(sin(float(idx) * 12.9898) * 43758.5453);
        float dM = mpDiameter(seed);
        float vt = terminalVel(dM);
        float vNow = d.velState.y / uPxPerMeter;
        float mR = RHO_W * 0.5236 * dM * dM * dM;
        float area = PI_F * 0.25 * dM * dM;
        float cd = 0.55;
        float fDrag = 0.5 * RHO_AIR * cd * area * vNow * vNow;
        float acc = G_ACC - fDrag / max(1.0e-12, mR);
        vNow = min(vt, vNow + acc * uDt);
        d.velState.y = vNow * uPxPerMeter;
        float windX = (vnoise(vec2(uTime * 0.21, 1.3)) - 0.5) * 0.9;
        d.velState.x = mix(d.velState.x, windX, clamp(uDt * 1.5, 0.0, 1.0));
        d.posVol.xy += d.velState.xy * uPxPerMeter * uDt;
        float landY = h21(vec2(float(idx) * 7.3, 2.9)) * uSimSize.y;
        if (d.posVol.y >= landY) {
            d.posVol.y = landY;
            d.velState.z = 1.0;
            float aImp = 0.5 * dM;
            float we = RHO_W * vNow * vNow * dM / SIGMA;
            float oh = MU_W / sqrt(max(1.0e-9, RHO_W * SIGMA * dM));
            float splashN = clamp((we - 40.0) / 260.0, 0.0, 1.0);
            float bSat = h21(vec2(float(idx) * 4.4, 8.1));
            uint spawned = 0u;
            uint maxSat = uint(min(6.0, 1.0 + splashN * 5.0));
            for (uint k = 384u; k < uint(drops.length()); k++) {
                if (spawned >= maxSat) break;
                if (drops[k].velState.w >= 0.5) continue;
                float ang = bSat * 6.28318 + float(k) * 2.399;
                float spdM = sqrt(max(0.0, 2.0 * SIGMA * (1.0 - contactCosCb()) / (RHO_W * max(1.0e-5, dM)))) * 0.42;
                spdM *= mix(0.55, 1.35, h21(vec2(float(k) * 9.7, float(idx) * 1.3)));
                float rSatM = dM * mix(0.06, 0.16, h21(vec2(float(k) * 5.1, 3.3)));
                float rSmPx = max(1.5, rSatM * uPxPerMeter);
                drops[k].posVol = vec4(d.posVol.xy, 0.5236 * rSatM * rSatM * rSatM, rSmPx);
                drops[k].velState = vec4(cos(ang) * spdM * uPxPerMeter, -abs(sin(ang)) * spdM * uPxPerMeter * 0.55, 0.0, 1.0);
                spawned++;
            }
            d.velState.xy = vec2(0.0);
            d.velState.z = 1.0;
            float volNew = 0.5236 * dM * dM * dM * 0.55;
            bool absorbed = false;
            for (int pk = 0; pk < 12; pk++) {
                uint q = (idx * 7u + uint(pk) * 37u + 1u) % max(1u, uint(drops.length()));
                if (q == idx) continue;
                if (drops[q].velState.w < 0.5) continue;
                if (drops[q].velState.z < 0.5) continue;
                float dq = length(drops[q].posVol.xy - d.posVol.xy);
                if (dq < (drops[q].posVol.w + 0.5 * dM * uPxPerMeter) * 1.05 && h21(vec2(float(idx), float(q) * 1.7)) > 0.45) {
                    drops[q].posVol.z += volNew;
                    float rq = pow(max(1.0e-12, drops[q].posVol.z) * 0.2387, 0.3333);
                    float aq = rq * max(0.28, sin(thRadGlobal()));
                    drops[q].posVol.w = max(1.0, aq * uPxPerMeter);
                    drops[q].velState.z = 1.0;
                    if (h21(vec2(float(q), uTime * 0.37)) > 0.55) {
                        drops[q].velState.y = max(drops[q].velState.y, 0.004 * uPxPerMeter);
                    }
                    absorbed = true;
                    d.velState.w = 0.0;
                    d.posVol.z = 0.0;
                    break;
                }
            }
            if (!absorbed) {
                d.posVol.z = volNew;
            }
        }
        drops[idx] = d;
        return;
    }

    float vol = max(1.0e-12, d.posVol.z);
    float rEquiv = pow(vol * (3.0 / (4.0 * PI_F)), 1.0 / 3.0);
    float dDiam = 2.0 * rEquiv;
    float cosCb = contactCosCb();
    float thRad = acos(clamp(cosCb, -0.99, 0.99));
    float aBase = rEquiv * max(0.28, sin(thRad));
    float dust = dustField(d.posVol.xy);
    float aM = aBase * (1.0 + 0.22 * dust);
    float aPx = max(1.0, aM * uPxPerMeter);
    float mass = RHO_W * vol;
    float fGrav = mass * G_ACC;
    float fPin = furmidgePin(aM, dust);
    vec2 vVec = d.velState.xy / uPxPerMeter;
    float vMag = length(vVec);
    float fViscMag = huhScriven(aM, vMag, thRad);
    float vTanner = tannerVel(aM, thRad);

    float drive = fGrav - fPin;
    if (drive > 0.0) {
        vec2 dirDown = vec2(0.0, 1.0);
        float target = min(vTanner, sqrt(max(0.0, 2.0 * drive / max(1.0e-12, mass)) * 2.0e-3));
        float slipBand = mix(0.35, 0.85, dust);
        float vAlong = vVec.y;
        if (vAlong < target * slipBand) {
            float stickDecay = 1.0 - clamp(uDt * mix(3.0, 14.0, dust), 0.0, 1.0);
            vVec.y *= stickDecay;
        } else {
            float accNet = (drive - fViscMag) / max(1.0e-12, mass);
            vVec.y = clamp(vVec.y + accNet * uDt, 0.0, max(0.01, target * 1.35));
        }
        float defect = vnoise(d.posVol.xy * 0.031 + 7.7) - 0.5;
        float pinDeflect = defect * SIGMA * aM * 3.4 / max(1.0e-12, mass);
        float gradX = sin(d.posVol.x * 0.015 - 0.15) - sin(d.posVol.x * 0.015 + 0.15);
        float marangoni = D_GAMMA_DT * 0.6 * aM * aM * 2.0 * PI_F / max(1.0e-12, mass);
        float fNetX = gradX * SIGMA * aM * 2.2 / max(1.0e-12, mass) + pinDeflect * 0.35 + marangoni;
        vVec.x = clamp(vVec.x + fNetX * uDt, -0.06, 0.06);
        vVec.x *= 1.0 - clamp(uDt * 2.0, 0.0, 1.0);
        d.velState.xy = vVec * uPxPerMeter;
        float stretch = clamp(1.0 + vVec.y * 6.5, 1.0, 1.75);
        d.velState.z = stretch;
    } else {
        float relax = clamp(uDt * mix(6.0, 22.0, dust), 0.0, 1.0);
        d.velState.xy *= (1.0 - relax);
        d.velState.z = mix(d.velState.z, 1.0, relax);
    }

    float dxPx = d.velState.x * uDt;
    float dyPx = d.velState.y * uDt;
    d.posVol.x = clamp(d.posVol.x + dxPx, aPx, max(aPx + 1.0, uSimSize.x - aPx));
    d.posVol.y += dyPx;
    d.posVol.w = aPx;

    float speedM = length(d.velState.xy) / uPxPerMeter;
    if (speedM > 0.0008) {
        float capNum = MU_W * speedM;
        float caNum = capNum / SIGMA;
        float hFilm = 0.945 * aM * pow(max(1.0e-12, caNum), 0.6667);
        float dsM = length(vec2(dxPx, dyPx)) / uPxPerMeter;
        float llD = 2.0 * aM * hFilm * 26.0 * dsM;
        vol = max(1.0e-12, vol - llD);
    }

    float evap = PI_F * dDiam * dDiam * 0.5 * stefanRate(dDiam) * uDt;
    vol = max(1.0e-12, vol - evap);

    uint nCount = uint(drops.length());
    for (uint j = 0u; j < nCount; j++) {
        if (j == idx) continue;
        Drop other = drops[j];
        if (other.velState.w < 0.5) continue;
        if (other.velState.z < 0.5) continue;
        float distPx = length(d.posVol.xy - other.posVol.xy);
        float sumPx = aPx + other.posVol.w;
        if (distPx < sumPx * 0.92) {
            if (idx < j) {
                float v1 = vol;
                float v2 = max(1.0e-12, other.posVol.z);
                float m1 = RHO_W * v1;
                float m2 = RHO_W * v2;
                float vNew = v1 + v2;
                float rNew = pow(vNew * (3.0 / (4.0 * PI_F)), 1.0 / 3.0);
                float aNew = rNew * max(0.28, sin(thRad));
                vec2 p1 = d.velState.xy;
                vec2 p2 = other.velState.xy;
                vec2 vMom = (p1 * m1 + p2 * m2) / max(1.0e-12, m1 + m2);
                float released = SIGMA * 4.0 * PI_F * (pow(max(1.0e-12, v1) * 0.75 / PI_F, 0.6667) + pow(max(1.0e-12, v2) * 0.75 / PI_F, 0.6667) - pow(vNew * 0.75 / PI_F, 0.6667));
                float kick = clamp(released / max(1.0e-12, mass * 40.0), 0.0, 0.08) * uPxPerMeter;
                d.posVol.z = vNew;
                d.velState.y = max(vMom.y, 0.0) + kick * 0.6;
                d.velState.x = vMom.x + (h21(vec2(float(j), uTime)) - 0.5) * kick;
                d.posVol.w = aNew * uPxPerMeter;
                drops[j].velState.w = 0.0;
            }
        }
    }

    float aPxR = d.posVol.w;
    int rad = int(clamp(aPxR * 1.12, 2.0, 56.0));
    ivec2 c = ivec2(d.posVol.xy);
    float stretchZ = max(1.0, d.velState.z);
    float capScale = (speedM > 0.0008) ? clamp(aPxR / 64.0, 0.14, 0.30) : clamp(aPxR / 48.0, 0.16, 0.55);
    for (int oy = -rad; oy <= rad; oy++) {
        for (int ox = -rad; ox <= rad; ox++) {
            ivec2 p = c + ivec2(ox, oy);
            if (p.x < 0 || p.y < 0 || p.x >= int(uSimSize.x) || p.y >= int(uSimSize.y)) continue;
            vec2 q = vec2(float(ox), float(oy) / stretchZ) / max(1.0, aPxR);
            float r2 = dot(q, q);
            if (r2 < 1.0) {
                float hCap = sqrt(1.0 - r2) * capScale;
                int depthI = int(clamp(hCap * 1023.0, 1.0, 1023.0));
                int packedM = (depthI << 12) | int(idx & 0xFFFu);
                imageAtomicMax(splat, p, packedM);
            }
        }
    }

    if (speedM > 0.0008) {
        float tailLen = clamp(speedM * uPxPerMeter * 0.10, 2.0, 9.0);
        int tLen = int(tailLen);
        for (int ty = 1; ty <= tLen; ty++) {
            float fall = 1.0 - float(ty) / tailLen;
            int yy = c.y + ty;
            if (yy < 0 || yy >= int(uSimSize.y)) break;
            for (int ox = -1; ox <= 1; ox++) {
                int xx = c.x + ox;
                if (xx < 0 || xx >= int(uSimSize.x)) continue;
                float wgt = exp(-abs(float(ox)) / 1.3) * fall;
                int dT = int(clamp(wgt * 0.30 * 1023.0, 1.0, 1023.0));
                imageAtomicMax(splat, ivec2(xx, yy), (dT << 12) | int(idx & 0xFFFu));
            }
        }
    }

    if (d.posVol.y - aPxR > uSimSize.y || aPxR < 3.5 || d.posVol.z < 1.0e-11) {
        float fr1 = fract(sin(float(idx) * 12.9898 + uTime * 78.233) * 43758.5453);
        float fr2 = fract(sin(float(idx) * 93.989 + uTime * 47.233) * 24634.6345);
        d.posVol.x = fr1 * uSimSize.x;
        d.posVol.y = -20.0 - fr2 * 420.0;
        d.velState.xy = vec2(0.0);
        d.velState.z = 0.0;
        float seedD = fract(sin(float(idx) * 45.164 + uTime * 12.989) * 12345.678);
        float dNew = mpDiameter(seedD);
        d.posVol.z = 0.5236 * dNew * dNew * dNew;
        d.posVol.w = max(1.0, 0.5 * dNew * uPxPerMeter);
        d.velState.w = 1.0;
    }
    drops[idx] = d;
}
"""

private const val HEIGHTFIELD_PDE_CS = """#version 320 es
precision highp float;
precision highp int;
precision highp image2D;
layout(local_size_x = 16, local_size_y = 16) in;

layout(rgba16f, binding = 0) uniform readonly highp image2D uHeightIn;
layout(r32i, binding = 1) uniform readonly highp iimage2D uSplat;
layout(rgba16f, binding = 2) uniform writeonly highp image2D uHeightOut;

uniform float uDt;

void main() {
    ivec2 p = ivec2(gl_GlobalInvocationID.xy);
    ivec2 sz = imageSize(uHeightIn);
    if (p.x >= sz.x || p.y >= sz.y) return;

    vec4 src = imageLoad(uHeightIn, p);
    float splatH = float(imageLoad(uSplat, p).r >> 12) / 1023.0;

    vec4 l = imageLoad(uHeightIn, max(ivec2(0), p - ivec2(1, 0)));
    vec4 r = imageLoad(uHeightIn, min(sz - 1, p + ivec2(1, 0)));
    vec4 u = imageLoad(uHeightIn, max(ivec2(0), p - ivec2(0, 1)));
    vec4 dn = imageLoad(uHeightIn, min(sz - 1, p + ivec2(0, 1)));

    float lap = (l.r + r.r + u.r + dn.r) * 0.25 - src.r;
    float vel = src.b + lap * 16.0 * uDt;
    vel *= exp(-3.6 * uDt);
    float wave = vel * uDt;
    float phys = max(src.r, splatH);
    float physWet = max(src.g, splatH > 0.02 ? 1.0 : 0.0);
    float evapK = 0.010 * (1.0 + 1.2 * exp(-phys * 10.0));
    float h = max(0.0, phys + wave - evapK * uDt);

    imageStore(uHeightOut, p, vec4(h, physWet * exp(-0.075 * uDt), vel, 1.0));
}
"""

private const val CLEAR_SPLAT_CS = """#version 320 es
precision highp float;
precision highp int;
precision highp image2D;
layout(local_size_x = 16, local_size_y = 16) in;
layout(r32i, binding = 0) uniform writeonly highp iimage2D uSplatBuf;
void main() {
    ivec2 p = ivec2(gl_GlobalInvocationID.xy);
    ivec2 sz = imageSize(uSplatBuf);
    if (p.x >= sz.x || p.y >= sz.y) return;
    imageStore(uSplatBuf, p, ivec4(0));
}
"""

private const val CLEAR_CAUSTICS_CS = """#version 320 es
precision highp float;
precision highp int;
precision highp image2D;
layout(local_size_x = 16, local_size_y = 16) in;
layout(r32i, binding = 0) uniform writeonly highp iimage2D uCausticBuf;
void main() {
    ivec2 p = ivec2(gl_GlobalInvocationID.xy);
    ivec2 sz = imageSize(uCausticBuf);
    if (p.x >= sz.x || p.y >= sz.y) return;
    imageStore(uCausticBuf, p, ivec4(0));
}
"""

private const val CAUSTICS_PHOTON_CS = """#version 320 es
precision highp float;
precision highp int;
precision highp image2D;
layout(local_size_x = 16, local_size_y = 16) in;

layout(rgba16f, binding = 0) uniform readonly highp image2D uHeightIn;
layout(r32i, binding = 1) uniform highp iimage2D uCausticBuf;

void splat(ivec2 sz, vec2 pos, int shift) {
    ivec2 px = ivec2(clamp(pos, vec2(0.0), vec2(sz - 1)));
    imageAtomicAdd(uCausticBuf, px, 4 << shift);
}

void main() {
    ivec2 p = ivec2(gl_GlobalInvocationID.xy);
    ivec2 sz = imageSize(uHeightIn);
    if (p.x >= sz.x || p.y >= sz.y) return;

    float hC = imageLoad(uHeightIn, p).r;
    if (hC < 0.02) return;

    float hL = imageLoad(uHeightIn, max(ivec2(0), p - ivec2(1, 0))).r;
    float hR = imageLoad(uHeightIn, min(sz - 1, p + ivec2(1, 0))).r;
    float hU = imageLoad(uHeightIn, max(ivec2(0), p - ivec2(0, 1))).r;
    float hD = imageLoad(uHeightIn, min(sz - 1, p + ivec2(0, 1))).r;

    vec3 N = normalize(vec3((hL - hR) * 4.5, (hU - hD) * 4.5, 1.0));
    vec3 L = normalize(vec3(0.45, -0.55, -0.70));
    float dist = hC * 18.0 + 12.0;

    vec3 rR = refract(L, N, 1.0 / 1.331);
    vec3 rG = refract(L, N, 1.0 / 1.333);
    vec3 rB = refract(L, N, 1.0 / 1.338);

    splat(sz, vec2(p) + (rR.xy / min(-0.2, rR.z)) * dist, 20);
    splat(sz, vec2(p) + (rG.xy / min(-0.2, rG.z)) * dist, 10);
    splat(sz, vec2(p) + (rB.xy / min(-0.2, rB.z)) * dist, 0);
}
"""

private const val FULLSCREEN_VS = """#version 320 es
out vec2 vUv;
uniform vec2 uResolution;
void main() {
    vec2 pos = vec2(float((gl_VertexID << 1) & 2), float(gl_VertexID & 2));
    vUv = pos;
    gl_Position = vec4(pos * 2.0 - 1.0, 0.0, 1.0);
}
"""

private const val FINAL_OPTICS_FS = """#version 320 es
#extension GL_OES_EGL_image_external_essl3 : require
precision highp float;
precision highp int;
precision highp sampler2D;
precision highp image2D;
precision highp samplerExternalOES;
in vec2 vUv;
out vec4 fragColor;
uniform samplerExternalOES uUiTex;
uniform sampler2D uHeightTex;
uniform mat4 uStMatrix;
layout(r32i, binding = 2) uniform readonly highp iimage2D uCausticBuf;
uniform vec2 uResolution;
uniform float uGlassThicknessPx;
uniform float uTime;

vec2 uiUv(vec2 uv) {
    return clamp((uStMatrix * vec4(uv, 0.0, 1.0)).xy, vec2(0.001), vec2(0.999));
}

float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

vec2 hash22(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * vec3(0.1031, 0.1030, 0.0973));
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.xx + p3.yz) * p3.zy);
}

float valueNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(
        mix(hash12(i), hash12(i + vec2(1.0, 0.0)), u.x),
        mix(hash12(i + vec2(0.0, 1.0)), hash12(i + vec2(1.0, 1.0)), u.x),
        u.y
    );
}

float fbmClouds(vec2 p) {
    float v = 0.0;
    float amp = 0.5;
    for (int i = 0; i < 4; i++) {
        v += amp * valueNoise(p);
        p = p * 2.03 + vec2(13.7, 7.3);
        amp *= 0.5;
    }
    return v;
}

float D_GGX(float NdotH, float roughness) {
    float a = roughness * roughness;
    float a2 = a * a;
    float d = (NdotH * NdotH) * (a2 - 1.0) + 1.0;
    return a2 / (3.14159265 * d * d);
}

float hfS(float a, float b, float t) {
    return smoothstep(a, b, t);
}
vec3 hfN13(float p) {
    vec3 p3 = fract(vec3(p) * vec3(0.1031, 0.11369, 0.13787));
    p3 += dot(p3, p3.yzx + 19.19);
    return fract(vec3((p3.x + p3.y) * p3.z, (p3.x + p3.z) * p3.y, (p3.y + p3.z) * p3.x));
}
float hfN(float t) {
    return fract(sin(t * 12345.564) * 7658.76);
}
float hfSaw(float b, float t) {
    return hfS(0.0, b, t) * hfS(1.0, b, t);
}
vec2 hfDropLayer(vec2 uv, float t) {
    vec2 UV = uv;
    vec2 uvMod = uv;
    uvMod.y += t;
    vec2 a = vec2(6.0, 1.0);
    vec2 grid = a * 2.0;
    vec2 id = floor(uvMod * grid);
    float colShift = hfN(id.x);
    uvMod.y += colShift;
    id = floor(uvMod * grid);
    vec3 n = hfN13(id.x * 35.2 + id.y * 2376.1);
    vec2 st = fract(uvMod * grid) - vec2(0.5, 0.0);
    float x = n.x - 0.5;
    float y = UV.y * 20.0;
    float wiggle = sin(y + sin(y));
    x = x + wiggle * (0.5 - abs(x)) * (n.z - 0.5);
    x = x * 0.7;
    float scaledT = t / 0.75;
    float ti = fract(scaledT + n.z);
    bool useSaw = n.y > 0.5;
    float speed = 0.5 + n.x;
    if (useSaw) {
        y = (hfSaw(0.85, ti) - 0.5) * 0.9 + 0.5;
    } else {
        y = fract(ti * speed) * 0.9 + 0.05;
    }
    vec2 p = vec2(x, y);
    float d = length((st - p) * a.yx);
    float mainDrop = hfS(0.4, 0.0, d);
    float r = sqrt(hfS(1.0, y, st.y));
    float cd = abs(st.x - x);
    float trail = hfS(0.23 * r, 0.15 * r * r, cd);
    float trailFront = hfS(-0.02, 0.02, st.y - y);
    trail = trail * trailFront * r * r;
    float y2 = UV.y;
    float trail2 = hfS(0.2 * r, 0.0, cd);
    float droplets = max(0.0, sin(y2 * (1.0 - y2) * 120.0) - st.y) * trail2 * trailFront * n.z;
    y2 = fract(y2 * 10.0) + (st.y - 0.5);
    float dd = length(st - vec2(x, y2));
    droplets = hfS(0.3, 0.0, dd);
    float m = mainDrop + droplets * r * trailFront;
    return vec2(m, trail);
}
float hfStaticDrops(vec2 uv, float t) {
    vec2 uvMod = uv * 40.0;
    vec2 id = floor(uvMod);
    uvMod = fract(uvMod) - 0.5;
    vec3 n = hfN13(id.x * 107.45 + id.y * 3543.654);
    vec2 p = (n.xy - 0.5) * 0.7;
    float d = length(uvMod - p);
    float fade = hfSaw(0.025, fract(t + n.z));
    float c = hfS(0.3, 0.0, d) * fract(n.z * 10.0) * fade;
    return c;
}
vec2 hfDrops(vec2 uv, float t, float l0, float l1, float l2) {
    float s = hfStaticDrops(uv, t) * l0;
    vec2 m1 = hfDropLayer(uv * 1.35, t) * l1;
    vec2 m2 = hfDropLayer(uv * 2.55, t) * l2;
    float c = s + m1.x + m2.x;
    c = hfS(0.30, 1.0, c);
    return vec2(c, max(m1.y * l0, m2.y * l1));
}
vec2 centeredUVToUv(vec2 cuv) {
    vec2 px = cuv * uResolution.y + 0.5 * uResolution;
    return px / uResolution;
}

vec3 hfGlass(vec3 baseCol, vec2 centeredUV, float t) {
    float staticDrops = 1.0;
    float layer1 = 0.8;
    float layer2 = 0.5;
    vec2 c = hfDrops(centeredUV, t, staticDrops, layer1, layer2);
    float dropMask = c.x;
    vec2 e = vec2(0.0015, 0.0);
    float cx = hfDrops(centeredUV + e, t, staticDrops, layer1, layer2).x;
    float cy = hfDrops(centeredUV + e.yx, t, staticDrops, layer1, layer2).x;
    vec2 n = vec2(cx - c.x, cy - c.x);
    vec2 refrUv = clamp(centeredUVToUv(centeredUV + n * 1.0), vec2(0.001), vec2(0.999));
    vec3 refracted = texture(uUiTex, uiUv(refrUv)).rgb;
    refracted *= 0.96;
    vec3 col = mix(baseCol, refracted, dropMask);
    float inner = smoothstep(0.55, 1.0, dropMask);
    float bead = smoothstep(0.30, 0.85, length(n) * 22.0) * inner;
    col *= mix(1.0, 0.90, bead);
    vec2 lightDir = normalize(vec2(-0.45, -0.89));
    vec2 nn = normalize(n + vec2(0.0001));
    float spec = max(0.0, dot(nn, lightDir));
    col += vec3(0.90, 0.93, 0.97) * pow(spec, 120.0) * inner * 0.26;
    return col;
}

vec3 sampleRainyEnv(vec3 R) {
    float up = clamp(-R.y * 0.5 + 0.5, 0.0, 1.0);
    vec3 sky = mix(vec3(0.20, 0.24, 0.30), vec3(0.75, 0.82, 0.89), smoothstep(0.18, 0.52, up));
    sky = mix(sky, vec3(0.42, 0.51, 0.62), smoothstep(0.50, 0.96, up));
    if (R.y < 0.05) {
        vec2 uv = R.xz / max(0.18, -R.y + 0.22) + vec2(uTime * 0.018, uTime * 0.008);
        float c = fbmClouds(uv * 1.4);
        sky = mix(sky, mix(vec3(0.48, 0.54, 0.62), vec3(0.84, 0.89, 0.94), smoothstep(0.32, 0.78, c)), 0.65);
    }
    float sunGlow = pow(max(dot(R, normalize(vec3(-0.58, 0.68, 0.45))), 0.0), 10.0);
    return sky + vec3(0.24, 0.27, 0.30) * sunGlow;
}

vec3 unpackCaustic(ivec2 px) {
    int packed = imageLoad(uCausticBuf, clamp(px, ivec2(0), ivec2(uResolution) - 1)).r;
    int r10 = packed / 1048576;
    int g10 = (packed - r10 * 1048576) / 1024;
    int b10 = packed - r10 * 1048576 - g10 * 1024;
    return clamp(vec3(float(r10) / 32.0, float(g10) / 32.0, float(b10) / 32.0), 0.0, 1.6);
}

vec3 sampleSmoothCaustics(ivec2 px) {
    vec3 sum = vec3(0.0);
    for (int dy = -1; dy <= 1; dy++) {
        for (int dx = -1; dx <= 1; dx++) {
            float w = (dx == 0 && dy == 0) ? 0.25 : ((abs(dx) + abs(dy) == 1) ? 0.125 : 0.0625);
            sum += unpackCaustic(px + ivec2(dx, dy)) * w;
        }
    }
    return sum;
}

void main() {
    vec2 fragPx = vUv * uResolution;
    vec4 base = texture(uUiTex, uiUv(vUv));
    base.rgb *= vec3(0.986, 0.996, 1.020);
    vec3 col = base.rgb;
    ivec2 pxCoord = ivec2(clamp(fragPx, vec2(0.0), uResolution - 1.0));
    vec3 rgbCaustic = sampleSmoothCaustics(pxCoord);
    col *= (vec3(1.0) + rgbCaustic * 0.22);

    for (int layer = 0; layer < 5; layer++) {
        float fl = float(layer);
        float t5 = fl * 0.25;
        float spacing = mix(118.0, 34.0, t5);
        float baseSpeed = mix(760.0, 2300.0, t5);
        float lenY = mix(58.0, 210.0, t5);
        float slant = mix(0.04, 0.17, t5);
        float bright = mix(0.09, 0.24, t5);
        float wpx = mix(0.7, 1.9, t5);
        float cellX = floor(fragPx.x / spacing);
        for (int ox = -2; ox <= 2; ox++) {
            float cx = cellX + float(ox);
            float rnd = hash12(vec2(cx, fl * 37.0 + 7.0));
            float rnd2 = hash12(vec2(cx, fl * 91.0 + 3.0));
            float rnd3 = hash12(vec2(cx, fl * 17.0 + 53.0));
            if (rnd3 > mix(0.62, 0.30, t5)) continue;
            float jitter = (rnd - 0.5) * 0.95;
            float xPos = (cx + 0.5 + jitter) * spacing;
            float spd = baseSpeed * mix(0.55, 1.75, rnd2);
            float lenJ = lenY * mix(0.45, 1.7, rnd3);
            float phase = fract(uTime * spd / (uResolution.y + lenJ * 3.5) + rnd2);
            float yPos = -lenJ + phase * (uResolution.y + lenJ * 3.5);
            float dyr = fragPx.y - yPos;
            if (dyr > -lenJ && dyr < lenJ * 0.22) {
                float dxr = fragPx.x - (xPos + dyr * slant);
                float wj = wpx * mix(0.6, 1.6, rnd);
                float line = exp(-abs(dxr) / wj);
                float along = clamp((dyr + lenJ) / lenJ, 0.0, 1.0);
                float fade = smoothstep(0.0, 0.20, along) * smoothstep(1.0, 0.50, along);
                float head = smoothstep(0.10, 0.0, along);
                float bri = bright * mix(0.45, 1.35, rnd2);
                col += vec3(0.62, 0.70, 0.82) * line * fade * (bri + head * 0.12);
            }
        }
    }
vec2 centeredUV = (fragPx - 0.5 * uResolution) / uResolution.y;
    col = hfGlass(col, centeredUV, uTime * 0.2);
    float hC = texture(uHeightTex, vUv).r;
    if (hC > 0.009) {
        vec2 texel = 1.0 / uResolution;
        float hL = texture(uHeightTex, vUv - vec2(texel.x * 2.0, 0.0)).r;
        float hR = texture(uHeightTex, vUv + vec2(texel.x * 2.0, 0.0)).r;
        float hU = texture(uHeightTex, vUv + vec2(0.0, texel.y * 2.0)).r;
        float hD = texture(uHeightTex, vUv - vec2(0.0, texel.y * 2.0)).r;
        vec3 N = normalize(vec3((hL - hR) * 4.5, (hU - hD) * 4.5, 1.0));
        vec3 V_in = vec3(0.0, 0.0, -1.0);
        vec3 V_out = vec3(0.0, 0.0, 1.0);
        vec3 trG = refract(V_in, N, 1.0 / 1.3330);
        vec2 refr = (trG.xy / max(0.14, abs(trG.z))) * (hC * hC) * 62.0 / uResolution;
        vec3 wetCol = texture(uUiTex, uiUv(vUv + refr)).rgb;
        float fresW = 0.0204 + 0.9796 * pow(1.0 - clamp(dot(N, V_out), 0.0, 1.0), 5.0);
        vec3 ggxW = vec3(1.0) * min(1.2, D_GGX(max(0.0, dot(N, normalize(normalize(vec3(-0.62, 0.68, 0.39)) + V_out))), 0.06) * 0.030);
        wetCol += ggxW;
        wetCol = mix(wetCol, sampleRainyEnv(reflect(-V_out, N)), clamp(fresW * 0.85, 0.0, 0.7));
        col = mix(col, wetCol, smoothstep(0.009, 0.095, hC) * 0.55);
    }

    if (isnan(col.r) || isnan(col.g) || isnan(col.b) || isinf(col.r) || isinf(col.g) || isinf(col.b)) {
        col = base.rgb;
    }
    col = clamp(col, vec3(0.0), vec3(1.0));
    fragColor = vec4(col, base.a);
}
"""