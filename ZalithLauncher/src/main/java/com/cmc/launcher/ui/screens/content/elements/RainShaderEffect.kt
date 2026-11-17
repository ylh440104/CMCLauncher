package com.cmc.launcher.ui.screens.content.elements

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import com.cmc.launcher.setting.AllSettings
import kotlinx.coroutines.android.awaitFrame

private const val RAIN_AGSL = """
uniform shader content;
uniform float t;
uniform float2 resolution;

float hash12(float2 p) {
    float3 p3 = fract(float3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float2 hash22(float2 p) {
    float3 p3 = fract(float3(p.xyx) * float3(0.1031, 0.1030, 0.0973));
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.xx + p3.yz) * p3.zy);
}

float dropMask(float2 uv, float cellPx, float seed, float minR, float maxR) {
    float2 st = uv * (resolution.y / cellPx);
    float2 id = floor(st);
    float2 gv = fract(st) - 0.5;
    float2 rnd = hash22(id + seed);
    float2 p = gv - (rnd - 0.5) * 0.68;
    float r = mix(minR, maxR, rnd.y);
    float d = length(p) - r;
    d -= 0.15 * r * smoothstep(-0.5 * r, 0.8 * r, -p.y);
    return smoothstep(0.015, -0.045, d);
}

float3 dropNormal(float2 uv, float cellPx, float seed, float minR, float maxR, float e) {
    float hC = dropMask(uv, cellPx, seed, minR, maxR);
    float hR = dropMask(uv + float2(e, 0.0), cellPx, seed, minR, maxR);
    float hU = dropMask(uv + float2(0.0, e), cellPx, seed, minR, maxR);
    return float3((hR - hC) / e, (hU - hC) / e, 1.0);
}

float streaks(float2 uv, float tt) {
    float s = 0.0;
    for (int k = 0; k < 3; k++) {
        float fk = float(k);
        float speed = 0.55 + fk * 0.38;
        float scaleX = 30.0 + fk * 22.0;
        float x = uv.x * scaleX;
        float col = floor(x);
        float h = hash12(float2(col, fk * 91.7));
        float y0 = fract(uv.y * (0.5 + h * 0.7) + tt * speed * (0.75 + h * 0.5) + h * 19.0);
        float m = smoothstep(0.0, 0.35, y0) * (1.0 - smoothstep(0.65, 1.0, y0));
        float inCol = smoothstep(0.0, 0.3, fract(x)) * (1.0 - smoothstep(0.7, 1.0, fract(x)));
        s += m * inCol * (0.045 - fk * 0.010);
    }
    return s;
}

half4 main(float2 fragPx) {
    float2 uv = fragPx / resolution.y;
    float tt = t;

    float m1 = dropMask(uv, 46.0, 3.1, 0.16, 0.30);
    float m2 = dropMask(uv, 78.0, 7.7, 0.18, 0.34);
    float m3 = dropMask(uv + float2(0.0, tt * 0.012), 118.0, 11.3, 0.22, 0.40);
    float m4 = dropMask(uv + float2(0.0, tt * 0.030), 168.0, 19.9, 0.26, 0.46);
    float m = max(max(m1, m2), max(m3, m4));

    if (m < 0.02) {
        half4 bg = content.eval(fragPx);
        float st = streaks(uv, tt);
        return half4(bg.rgb + half3(st), bg.a);
    }

    float e = 0.0035;
    float3 n1 = dropNormal(uv, 46.0, 3.1, 0.16, 0.30, e);
    float3 n2 = dropNormal(uv, 78.0, 7.7, 0.18, 0.34, e);
    float3 n3 = dropNormal(uv + float2(0.0, tt * 0.012), 118.0, 11.3, 0.22, 0.40, e);
    float3 n4 = dropNormal(uv + float2(0.0, tt * 0.030), 168.0, 19.9, 0.26, 0.46, e);
    float3 grad3 = n1 + n2 + n3 + n4;
    float2 grad = normalize(grad3.xz + 1e-5);
    float slope = clamp(length(grad3.xz) * 1.8, 0.0, 1.0);

    float2 refr = grad * slope * 14.0 * m;
    half4 bg = content.eval(fragPx + refr);
    float3 n = normalize(float3(grad.x * slope, 1.0, grad.y * slope));
    float3 L = normalize(float3(-0.5, 0.7, -0.5));
    float diff = clamp(dot(n, L) * 0.35 + 0.75, 0.0, 1.2);
    float spec = pow(clamp(dot(reflect(-L, n), float3(0.0, 1.0, 0.0)), 0.0, 1.0), 42.0);
    float rim = pow(1.0 - smoothstep(0.55, 1.0, m), 2.0) * m;

    half3 col = bg.rgb * diff;
    col = mix(col, bg.rgb * 1.12 + half3(0.05), clamp(rim * 0.6, 0.0, 0.55));
    col += half3(spec * 0.85 * m);

    float st = streaks(uv, tt);
    col += half3(st);

    return half4(col, bg.a);
}
"""

@Composable
fun Modifier.rainShaderEffect(enabled: Boolean = AllSettings.liquidGlass.state): Modifier {
    if (Build.VERSION.SDK_INT < 33 || !enabled) return this

    val shader = remember { RuntimeShader(RAIN_AGSL) }
    var time by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var last = 0L
        while (true) {
            val frame = awaitFrame()
            if (last != 0L) {
                time += (frame - last) / 1_000_000_000f
            }
            last = frame
        }
    }

    return this.graphicsLayer {
        shader.setFloatUniform("t", time)
        shader.setFloatUniform(
            "resolution",
            size.width.coerceAtLeast(1f),
            size.height.coerceAtLeast(1f)
        )
        renderEffect = RenderEffect
            .createRuntimeShaderEffect(shader, "content")
            .asComposeRenderEffect()
    }
}