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

float dropD(float2 uv, float cellPx, float seed, float minR, float maxR, float present) {
    float2 st = uv * (resolution.y / cellPx);
    float2 id = floor(st);
    float2 gv = fract(st) - 0.5;
    float2 rnd = hash22(id + seed);
    if (rnd.x > present) return 1.0;
    float2 p = gv - (rnd - 0.5) * 0.5;
    float r = mix(minR, maxR, rnd.y);
    float d = length(p) - r;
    d -= 0.16 * r * smoothstep(-0.6 * r, 0.9 * r, -p.y);
    return d;
}

float3 dropNormalAt(float2 uv, float cellPx, float seed, float minR, float maxR, float present, float e) {
    float d0 = dropD(uv, cellPx, seed, minR, maxR, present);
    float dX = dropD(uv + float2(e, 0.0), cellPx, seed, minR, maxR, present);
    float dY = dropD(uv + float2(0.0, e), cellPx, seed, minR, maxR, present);
    float inside = smoothstep(0.01, -0.05, d0);
    return float3((dX - d0) / e, (dY - d0) / e, inside);
}

float streaks(float2 uv, float tt) {
    float s = 0.0;
    for (int k = 0; k < 2; k++) {
        float fk = float(k);
        float speed = 0.5 + fk * 0.35;
        float scaleX = 26.0 + fk * 18.0;
        float x = uv.x * scaleX;
        float col = floor(x);
        float h = hash12(float2(col, fk * 91.7));
        if (h > 0.55) continue;
        float y0 = fract(uv.y * (0.45 + h * 0.6) + tt * speed * (0.7 + h * 0.5) + h * 19.0);
        float m = smoothstep(0.0, 0.4, y0) * (1.0 - smoothstep(0.6, 1.0, y0));
        float inCol = smoothstep(0.0, 0.35, fract(x)) * (1.0 - smoothstep(0.65, 1.0, fract(x)));
        s += m * inCol * 0.05;
    }
    return s;
}

half4 main(float2 fragPx) {
    float2 uv = fragPx / resolution.y;
    float tt = t;

    float3 g1 = dropNormalAt(uv, 88.0, 3.1, 0.14, 0.24, 0.30, 0.0035);
    float3 g2 = dropNormalAt(uv, 132.0, 7.7, 0.16, 0.30, 0.22, 0.0035);
    float3 g3 = dropNormalAt(uv + float2(0.0, tt * 0.010), 176.0, 11.3, 0.20, 0.36, 0.15, 0.0035);

    float inside = max(max(g1.z, g2.z), g3.z);
    float2 grad = g1.xy * g1.z + g2.xy * g2.z + g3.xy * g3.z;

    if (inside < 0.01) {
        half4 bg = content.eval(fragPx);
        float st = streaks(uv, tt);
        return half4(bg.rgb + half3(st), bg.a);
    }

    float2 refr = -grad * 0.055;
    half4 bg = content.eval(fragPx + refr);

    float3 n = normalize(float3(grad * 0.55, 1.0));
    float3 L = normalize(float3(-0.45, 0.78, -0.45));
    float diff = clamp(dot(n, L) * 0.30 + 0.82, 0.0, 1.15);
    float spec = pow(clamp(dot(reflect(-L, n), float3(0.0, 1.0, 0.0)), 0.0, 1.0), 48.0);
    float rim = (1.0 - smoothstep(0.75, 1.0, inside)) * inside;

    half3 col = bg.rgb * diff;
    col = mix(col, bg.rgb * 1.10 + half3(0.045), clamp(rim * 0.45, 0.0, 0.45));
    col += half3(spec * 0.75 * inside);

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