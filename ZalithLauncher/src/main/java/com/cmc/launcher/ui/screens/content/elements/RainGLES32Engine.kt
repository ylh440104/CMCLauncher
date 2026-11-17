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
            captureUiToSurface()
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    init {
        glView.setRenderer(renderer)
        glView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        addView(contentView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(glView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
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

private class RainGLES32Renderer(context: Context) : GLSurfaceView.Renderer {

    companion object {
        private const val TAG = "RainGL"
        private const val MAX_DROPS = 512
    }

    private val pxPerMeter = (context.resources.displayMetrics.xdpi / 0.0254f).coerceAtLeast(12000f)

    @Volatile
    var uiExternalTexId = 0
        private set

    @Volatile
    private var surfaceTexture: SurfaceTexture? = null

    private val stMatrix = FloatArray(16)
    private var matrixReady = false

    private var width = 720
    private var height = 1610

    private var dropPhysicsProgram = 0
    private var heightFieldProgram = 0
    private var clearCausticsProgram = 0
    private var causticPhotonProgram = 0
    private var renderProgram = 0

    private var dropSsbo = 0
    private var heightTexA = 0
    private var heightTexB = 0
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

        // 1. DROP_PHYSICS_CS: 512 颗 SSBO 物理水滴（Furmidge 钉扎/Huh-Scriven 粘滞/聚并/LLD 沉积/Stefan 蒸发），球冠写入 outTex
        GLES32.glUseProgram(dropPhysicsProgram)
        GLES32.glBindBufferBase(GLES32.GL_SHADER_STORAGE_BUFFER, 0, dropSsbo)
        GLES32.glUniform2f(loc(dropPhysicsProgram, "uSimSize"), width.toFloat(), height.toFloat())
        GLES32.glUniform1f(loc(dropPhysicsProgram, "uPxPerMeter"), pxPerMeter)
        GLES32.glUniform1f(loc(dropPhysicsProgram, "uDt"), dt)
        GLES32.glUniform1f(loc(dropPhysicsProgram, "uTime"), elapsedSec)
        GLES32.glBindImageTexture(1, outTex, 0, false, 0, GLES32.GL_READ_WRITE, GLES32.GL_RGBA16F)
        GLES32.glDispatchCompute(MAX_DROPS / 64, 1, 1)
        GLES32.glMemoryBarrier(GLES32.GL_SHADER_STORAGE_BARRIER_BIT or GLES32.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT)

        // 2. HEIGHTFIELD_PDE_CS: 浅水毛细波演化 + 蒸发（in→out，保留物理写入的球冠）
        GLES32.glUseProgram(heightFieldProgram)
        GLES32.glBindImageTexture(0, inTex, 0, false, 0, GLES32.GL_READ_ONLY, GLES32.GL_RGBA16F)
        GLES32.glBindImageTexture(1, outTex, 0, false, 0, GLES32.GL_READ_WRITE, GLES32.GL_RGBA16F)
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

        // 5. FINAL_OPTICS_FS
        GLES32.glViewport(0, 0, width, height)
        GLES32.glUseProgram(renderProgram)

        GLES32.glActiveTexture(GLES32.GL_TEXTURE0)
        GLES32.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, uiExternalTexId)
        GLES32.glUniform1i(loc(renderProgram, "uUiTex"), 0)

        GLES32.glActiveTexture(GLES32.GL_TEXTURE1)
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, outTex)
        GLES32.glUniform1i(loc(renderProgram, "uHeightTex"), 1)

        GLES32.glBindImageTexture(2, causticIntTex, 0, false, 0, GLES32.GL_READ_ONLY, GLES32.GL_R32I)

        GLES32.glUniform2f(loc(renderProgram, "uResolution"), width.toFloat(), height.toFloat())
        GLES32.glUniform1f(loc(renderProgram, "uGlassThicknessPx"), 0.004f * pxPerMeter * 0.25f)
        GLES32.glUniform1f(loc(renderProgram, "uTime"), elapsedSec)
        GLES32.glUniformMatrix4fv(
            loc(renderProgram, "uStMatrix"), 1, false,
            if (matrixReady) stMatrix else IDENT, 0
        )

        GLES32.glBindVertexArray(quadVao)
        GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, 3)
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
            val rPx = when {
                i < 48 -> Random.nextFloat() * 22f + 26f
                i < 200 -> Random.nextFloat() * 12f + 13f
                else -> Random.nextFloat() * 6f + 5.5f
            }
            val rM = rPx / pxPerMeter
            data[base] = Random.nextFloat() * width
            data[base + 1] = Random.nextFloat() * height
            data[base + 2] = 1.35f * rM * rM * rM
            data[base + 3] = rPx
            data[base + 4] = 0f
            data[base + 5] = 0f
            data[base + 6] = 1f
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
        val texs = intArrayOf(heightTexA, heightTexB, causticIntTex)
        if (heightTexA != 0) GLES32.glDeleteTextures(3, texs, 0)
        GLES32.glGenTextures(3, texs, 0)
        heightTexA = texs[0]
        heightTexB = texs[1]
        causticIntTex = texs[2]

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
            Log.e(TAG, "$tag compile FAILED: ${GLES32.glGetShaderInfoLog(shader)}")
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
layout(local_size_x = 64) in;

struct Drop {
    vec4 posVol;
    vec4 velState;
};

layout(std430, binding = 0) buffer DropBuffer {
    Drop drops[];
};

layout(rgba16f, binding = 1) uniform image2D uHeightField;

uniform vec2 uSimSize;
uniform float uPxPerMeter;
uniform float uDt;
uniform float uTime;

const float RHO_W = 998.0;
const float SIGMA = 0.0728;
const float MU_W  = 0.001002;
const float G_ACC = 9.81;
const float LAMBDA_T = 1.0e-8;
const float COS_R_A = 0.49;
const float EVAP_DIFF = 2.8e-10;

void main() {
    uint idx = gl_GlobalInvocationID.x;
    if (idx >= drops.length()) return;

    Drop d = drops[idx];
    if (d.velState.w < 0.5) return;

    float vol = d.posVol.z;
    float rCurvM = pow(max(0.0, vol / 1.35), 1.0 / 3.0);
    float aM = max(1e-6, rCurvM * 0.73);
    float aPx = aM * uPxPerMeter;

    float mass = RHO_W * vol;
    float fGrav = mass * G_ACC;
    float fs = 1.0 + 0.30 * sin(d.posVol.x * 0.015) * cos(d.posVol.y * 0.015);
    float fPin = 3.14159265 * aM * SIGMA * COS_R_A * fs;

    float uSpeed = length(d.velState.xy);
    float lnTerm = log(max(2.0, aM / LAMBDA_T));
    float visc = 3.6 * MU_W * aM * lnTerm * 85.0;

    if (fGrav > fPin) {
        float fNetY = (fGrav - fPin * 0.90) - visc * d.velState.y;
        float gradX = sin(d.posVol.x * 0.015 - 0.15) - sin(d.posVol.x * 0.015 + 0.15);
        float fNetX = gradX * SIGMA * aM * 2.2 - visc * 1.6 * d.velState.x;
        d.velState.y = clamp(d.velState.y + (fNetY / max(mass, 1e-9)) * uDt, 0.003, 0.045);
        d.velState.x = clamp(d.velState.x + (fNetX / max(mass, 1e-9)) * uDt, -0.014, 0.014);
    } else {
        d.velState.xy *= 0.5;
    }

    float dx = d.velState.x * uPxPerMeter * uDt;
    float dy = d.velState.y * uPxPerMeter * uDt;
    d.posVol.x = clamp(d.posVol.x + dx, aPx, uSimSize.x - aPx);
    d.posVol.y += dy;
    d.posVol.w = aPx;
    d.velState.z = (uSpeed > 0.002) ? clamp(1.28 + d.velState.y * 11.5, 1.30, 1.60) : 1.0;

    if (uSpeed > 0.002) {
        float ca = (MU_W * uSpeed) / SIGMA;
        float hFilm = 0.945 * aM * pow(ca, 2.0 / 3.0);
        float dsM = length(vec2(dx, dy)) / uPxPerMeter;
        vol = max(0.0, vol - 2.0 * aM * (hFilm * 26.0) * dsM);
    }
    vol = max(0.0, vol - 6.28318 * aM * EVAP_DIFF * uDt);
    d.posVol.z = vol;

    for (uint j = 0u; j < drops.length(); j++) {
        if (j == idx) continue;
        Drop other = drops[j];
        if (other.velState.w < 0.5) continue;
        float dist = length(d.posVol.xy - other.posVol.xy);
        if (dist < (aPx + other.posVol.w) * 0.85 && idx < j) {
            d.posVol.z += other.posVol.z;
            d.velState.y = max(d.velState.y, other.velState.y) * 1.45 + 0.008;
            drops[j].velState.w = 0.0;
        }
    }

    int rad = int(clamp(aPx * 1.15, 2.0, 56.0));
    ivec2 c = ivec2(d.posVol.xy);
    float capScale = (uSpeed > 0.002) ? clamp(aPx / 64.0, 0.16, 0.30) : clamp(aPx / 48.0, 0.18, 0.55);
    for (int oy = -rad; oy <= rad; oy++) {
        for (int ox = -rad; ox <= rad; ox++) {
            ivec2 p = c + ivec2(ox, oy);
            if (p.x < 0 || p.y < 0 || p.x >= int(uSimSize.x) || p.y >= int(uSimSize.y)) continue;
            vec2 q = vec2(float(ox), float(oy) / d.velState.z) / max(1.0, aPx);
            float r2 = dot(q, q);
            if (r2 < 1.0) {
                float hCap = sqrt(1.0 - r2) * capScale;
                vec4 cur = imageLoad(uHeightField, p);
                cur.r = max(cur.r, hCap);
                cur.g = max(cur.g, 1.0);
                imageStore(uHeightField, p, cur);
            }
        }
    }

    if (d.posVol.y - aPx > uSimSize.y || aPx < 5.0) {
        float fr1 = fract(sin(float(idx) * 12.9898 + uTime * 78.233) * 43758.5453);
        float fr2 = fract(sin(float(idx) * 93.989 + uTime * 47.233) * 24634.6345);
        d.posVol.x = fr1 * uSimSize.x;
        d.posVol.y = -24.0;
        d.velState.y = 0.0;
        d.velState.x = 0.0;
        float rNewPx = mix(9.0, 20.0, fr2);
        float rNew = rNewPx / uPxPerMeter;
        d.posVol.z = 1.35 * rNew * rNew * rNew;
    }
    drops[idx] = d;
}
"""

private const val HEIGHTFIELD_PDE_CS = """#version 320 es
layout(local_size_x = 16, local_size_y = 16) in;

layout(rgba16f, binding = 0) uniform readonly image2D uHeightIn;
layout(rgba16f, binding = 1) uniform image2D uHeightOut;

uniform float uDt;

void main() {
    ivec2 p = ivec2(gl_GlobalInvocationID.xy);
    ivec2 sz = imageSize(uHeightIn);
    if (p.x >= sz.x || p.y >= sz.y) return;

    vec4 src = imageLoad(uHeightIn, p);
    vec4 c = imageLoad(uHeightOut, p);

    vec4 l = imageLoad(uHeightIn, max(ivec2(0), p - ivec2(1, 0)));
    vec4 r = imageLoad(uHeightIn, min(sz - 1, p + ivec2(1, 0)));
    vec4 u = imageLoad(uHeightIn, max(ivec2(0), p - ivec2(0, 1)));
    vec4 dn = imageLoad(uHeightIn, min(sz - 1, p + ivec2(0, 1)));

    float lap = (l.r + r.r + u.r + dn.r) * 0.25 - src.r;
    float vel = src.b + lap * 16.0 * uDt;
    vel *= exp(-3.6 * uDt);
    float wave = vel * uDt;

    float phys = max(src.r, c.r);
    float physWet = max(src.g, c.g);
    float h = max(0.0, phys + wave - 0.065 * uDt);

    imageStore(uHeightOut, p, vec4(h, physWet * exp(-0.075 * uDt), vel, 1.0));
}
"""

private const val CLEAR_CAUSTICS_CS = """#version 320 es
layout(local_size_x = 16, local_size_y = 16) in;
layout(r32i, binding = 0) uniform writeonly iimage2D uCausticBuf;
void main() {
    ivec2 p = ivec2(gl_GlobalInvocationID.xy);
    ivec2 sz = imageSize(uCausticBuf);
    if (p.x >= sz.x || p.y >= sz.y) return;
    imageStore(uCausticBuf, p, ivec4(0));
}
"""

private const val CAUSTICS_PHOTON_CS = """#version 320 es
layout(local_size_x = 16, local_size_y = 16) in;

layout(rgba16f, binding = 0) uniform readonly image2D uHeightIn;
layout(r32i, binding = 1) uniform iimage2D uCausticBuf;

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

in vec2 vUv;
out vec4 fragColor;

uniform samplerExternalOES uUiTex;
uniform sampler2D uHeightTex;
uniform mat4 uStMatrix;
layout(r32i, binding = 2) uniform readonly iimage2D uCausticBuf;
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
    float r = float((packed >> 20) & 0x3FF) / 32.0;
    float g = float((packed >> 10) & 0x3FF) / 32.0;
    float b = float(packed & 0x3FF) / 32.0;
    return clamp(vec3(r, g, b), 0.0, 1.6);
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

vec3 traceMultiBounceDroplet(vec3 V_in, vec3 N, float nWater, float R_px) {
    float nGlass = 1.50;
    vec3 ray = refract(V_in, N, 1.0 / nWater);
    vec3 rayGlass = refract(ray, vec3(0.0, 0.0, -1.0), nWater / nGlass);
    vec3 rayExit = refract(rayGlass, vec3(0.0, 0.0, -1.0), nGlass);
    if (dot(rayExit, rayExit) < 0.0001) {
        vec3 bounce1 = reflect(ray, vec3(0.0, 0.0, -1.0));
        vec3 exit2 = refract(bounce1, -N, nWater);
        if (dot(exit2, exit2) < 0.0001) return vec3(0.0, 0.0, 1.0);
        return vec3((exit2.xy / max(0.2, abs(exit2.z))) * (R_px * 1.8), 0.55);
    }
    vec2 glassShift = (rayGlass.xy / min(-0.25, rayGlass.z)) * uGlassThicknessPx;
    vec2 airShift = (rayExit.xy / min(-0.16, rayExit.z)) * (R_px * 3.85);
    return vec3(glassShift + airShift, 0.0);
}

vec3 renderStaticMicroDroplet(vec2 fragPx, vec2 centerPx, float aPx, vec3 baseCol) {
    vec2 q = (fragPx - centerPx) / aPx;
    float d = length(q);
    if (d > 1.15) return baseCol;
    if (d >= 1.0) {
        float rim = smoothstep(1.35, 0.0, (d - 1.0) * aPx);
        return mix(baseCol, vec3(0.95, 0.98, 1.0), rim * 0.42);
    }
    float z = sqrt(max(0.0006, 1.0 - d * d));
    vec3 N = normalize(vec3(q.x, -q.y, z));
    vec3 V_in = vec3(0.0, 0.0, -1.0);
    vec3 V_out = vec3(0.0, 0.0, 1.0);

    vec3 trR = traceMultiBounceDroplet(V_in, N, 1.331, aPx * 1.15);
    vec3 trG = traceMultiBounceDroplet(V_in, N, 1.333, aPx * 1.15);
    vec3 trB = traceMultiBounceDroplet(V_in, N, 1.338, aPx * 1.15);
    float tir = clamp((trR.z + trG.z + trB.z) * 0.3333, 0.0, 1.0);

    vec2 uvR = uiUv((centerPx + trR.xy) / uResolution);
    vec2 uvG = uiUv((centerPx + trG.xy) / uResolution);
    vec2 uvB = uiUv((centerPx + trB.xy) / uResolution);
    vec3 refracted = vec3(
        texture(uUiTex, vec2(uvR.x, 1.0 - uvR.y)).r,
        texture(uUiTex, vec2(uvG.x, 1.0 - uvG.y)).g,
        texture(uUiTex, vec2(uvB.x, 1.0 - uvB.y)).b
    );

    vec3 col = refracted * mix(1.12, 1.28, 1.0 - d * 0.65);
    vec3 L = normalize(vec3(-0.62, 0.68, 0.39));
    col *= (1.0 + dot(N, L) * 0.16);
    float fresnel = 0.02006 + (1.0 - 0.02006) * pow(1.0 - clamp(dot(N, V_out), 0.0, 1.0), 5.0);
    col = mix(col, sampleRainyEnv(reflect(-V_out, N)), clamp(fresnel * 0.68 + tir * 0.72, 0.0, 0.92));

    float ggx = D_GGX(max(dot(N, normalize(L + V_out)), 0.0), 0.055) * 0.045;
    col += vec3(1.0) * (min(1.5, ggx) + smoothstep(0.14, 0.01, length(q - vec2(-0.36, -0.38))) * 0.55);

    float rim = smoothstep(1.8, 0.0, abs((1.0 - d) * aPx - 0.5));
    return mix(col, vec3(0.97, 0.99, 1.0), rim * 0.88);
}

void main() {
    vec2 fragPx = vUv * uResolution;
    vec4 base = texture(uUiTex, uiUv(vUv));
    base.rgb *= vec3(0.986, 0.996, 1.020);
    vec3 col = base.rgb;

    ivec2 pxCoord = ivec2(clamp(fragPx, vec2(0.0), uResolution - 1.0));
    vec3 rgbCaustic = sampleSmoothCaustics(pxCoord);
    col *= (vec3(1.0) + rgbCaustic * 0.24);

    for (int i = 0; i < 6; i++) {
        float seed = float(i) * 19.37;
        float speed = mix(1.0, 2.0, hash12(vec2(seed, 1.0)));
        float tCycle = fract(uTime * speed + hash12(vec2(seed, 2.0)));
        float xPos = hash12(vec2(floor(uTime * speed + hash12(vec2(seed, 2.0))), seed)) * uResolution.x;
        float yTip = tCycle * (uResolution.y + 150.0);
        float len = mix(60.0, 130.0, hash12(vec2(seed, 3.0)));
        float dx = abs(fragPx.x - xPos);
        if (dx < 1.35) {
            float dy = yTip - (uResolution.y - fragPx.y);
            if (dy >= 0.0 && dy <= len) {
                float fade = smoothstep(0.0, 0.14, dy / len) * smoothstep(1.0, 0.08, dy / len) * smoothstep(1.35, 0.15, dx);
                col += vec3(0.065, 0.075, 0.095) * fade;
            }
        }
    }

    float cellSize = 44.0;
    vec2 grid = floor(fragPx / cellSize);
    for (int dy = -1; dy <= 1; dy++) {
        for (int dx = -1; dx <= 1; dx++) {
            vec2 cell = grid + vec2(float(dx), float(dy));
            vec2 rnd = hash22(cell + vec2(11.0, 19.0));
            if (rnd.x > 0.16) continue;
            vec2 center = (cell + 0.5) * cellSize + (hash22(cell * 5.7 + 11.0) - 0.5) * (cellSize * 0.76);
            float hSweep = texture(uHeightTex, center / uResolution).r;
            if (hSweep > 0.06) continue;
            float r = mix(5.0, 12.0, hash12(cell * 3.1 + 11.0)) * (1.0 + 0.015 * sin(uTime * 1.35 + rnd.y * 6.283));
            if (length(fragPx - center) <= r * 1.15) {
                col = renderStaticMicroDroplet(fragPx, center, r, col);
            }
        }
    }

    float hC = texture(uHeightTex, vUv).r;
    if (hC > 0.015) {
        vec2 texel = 1.0 / uResolution;
        float hL = texture(uHeightTex, vUv - vec2(texel.x * 2.0, 0.0)).r;
        float hR = texture(uHeightTex, vUv + vec2(texel.x * 2.0, 0.0)).r;
        float hD = texture(uHeightTex, vUv - vec2(0.0, texel.y * 2.0)).r;
        float hU = texture(uHeightTex, vUv + vec2(0.0, texel.y * 2.0)).r;

        vec3 N = normalize(vec3((hL - hR) * 4.5, (hU - hD) * 4.5, 1.0));
        vec3 V_in = vec3(0.0, 0.0, -1.0);
        vec3 V_out = vec3(0.0, 0.0, 1.0);
        vec3 L = normalize(vec3(-0.62, 0.68, 0.39));

        vec3 trR = traceMultiBounceDroplet(V_in, N, 1.331, hC * 28.0);
        vec3 trG = traceMultiBounceDroplet(V_in, N, 1.333, hC * 28.0);
        vec3 trB = traceMultiBounceDroplet(V_in, N, 1.338, hC * 28.0);
        float tir = clamp((trR.z + trG.z + trB.z) * 0.3333, 0.0, 1.0);

        vec2 uvR = uiUv(vUv + trR.xy / uResolution);
        vec2 uvG = uiUv(vUv + trG.xy / uResolution);
        vec2 uvB = uiUv(vUv + trB.xy / uResolution);

        vec3 refractCol = vec3(
            texture(uUiTex, uvR).r,
            texture(uUiTex, uvG).g,
            texture(uUiTex, uvB).b
        );

        vec3 beer = vec3(exp(-0.045 * hC), exp(-0.015 * hC), exp(-0.005 * hC));
        vec3 wetCol = refractCol * beer * (vec3(1.12) + rgbCaustic * 0.32);

        float fresnel = 0.02006 + (1.0 - 0.02006) * pow(1.0 - clamp(dot(N, V_out), 0.0, 1.0), 5.0);
        wetCol = mix(wetCol, sampleRainyEnv(reflect(-V_out, N)), clamp(fresnel * 0.72 + tir * 0.70, 0.0, 0.92));

        float wetMask = smoothstep(0.015, 0.07, hC);
        float coverage = smoothstep(0.05, 0.28, hC);
        wetCol = mix(wetCol, base.rgb * 1.05, clamp((coverage - 0.55) * 2.2, 0.0, 0.85));

        float ggx = D_GGX(max(dot(N, normalize(L + V_out)), 0.0), 0.055) * 0.045;
        wetCol += vec3(1.0) * min(1.5, ggx);

        float rim = smoothstep(0.06, 0.015, hC) * smoothstep(0.008, 0.03, hC);
        wetCol = mix(wetCol, vec3(0.96, 0.99, 1.0), rim * 0.80);

        col = mix(col, wetCol, smoothstep(0.015, 0.07, hC));
    }

    fragColor = vec4(col, base.a);
}
"""