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

float4 dropSdf(float2 fragPx, float cellPx, float seed, float rMin, float rMax, float present, float vy) {
    float2 st = fragPx / cellPx;
    st.y += vy;
    float2 id = floor(st);
    float2 gv = fract(st) - 0.5;
    float2 rnd = hash22(id + seed);
    if (rnd.x > present) return float4(1.0, 0.0, 0.0, 0.0);
    float2 center = (rnd - 0.5) * 0.52;
    float2 p = gv - center;
    float r = mix(rMin, rMax, rnd.y);
    float d = length(p) - r;
    d -= 0.14 * r * smoothstep(-0.6 * r, 0.9 * r, -p.y);
    return float4(d, p.x / max(r, 1e-4), p.y / max(r, 1e-4), rnd.y);
}

half4 shadeDrop(float4 info, float2 fragPx) {
    float d = info.x;
    float nx = info.y;
    float ny = info.z;
    float rnd = info.w;

    float inside = smoothstep(0.015, -0.06, d);
    float edge = smoothstep(0.045, 0.0, abs(d) - 0.01);

    float2 dir = normalize(float2(nx, ny) + 1e-5);
    float2 refr = fragPx + dir * (9.0 + 7.0 * rnd) * (0.55 + 0.45 * (1.0 - length(float2(nx, ny))));
    half4 bg = content.eval(refr);

    float3 n = normalize(float3(nx * 1.6, ny * 1.6, 1.0));
    float3 L = normalize(float3(-0.45, -0.35, 0.82));
    float diff = clamp(dot(n, L), 0.0, 1.0);
    float spec = pow(diff, 60.0);

    float bright = 1.0 + diff * 0.35 + spec * 1.6;

    half3 col = bg.rgb * bright;
    col += half3(edge * 0.85);
    col -= half3(smoothstep(0.30, 0.06, length(float2(nx, ny))) * 0.22 * (1.0 - edge));

    return half4(col, inside);
}

half4 main(float2 fragPx) {
    float tt = t;

    float4 i1 = dropSdf(fragPx, 92.0, 3.1, 0.10, 0.20, 0.34, 0.0);
    float4 i2 = dropSdf(fragPx, 140.0, 7.7, 0.13, 0.26, 0.24, tt * 0.008);
    float4 i3 = dropSdf(fragPx, 196.0, 11.3, 0.16, 0.34, 0.16, tt * 0.022);
    float4 i4 = dropSdf(fragPx, 260.0, 19.9, 0.20, 0.42, 0.10, tt * 0.045);

    half4 base = content.eval(fragPx);
    half4 outCol = base;
    float cov = 0.0;

    if (i4.x < 0.05) { half4 s = shadeDrop(i4, fragPx); outCol = mix(outCol, s, s.a); cov = max(cov, s.a); }
    if (i3.x < 0.05) { half4 s = shadeDrop(i3, fragPx); outCol = mix(outCol, s, s.a); cov = max(cov, s.a); }
    if (i2.x < 0.05) { half4 s = shadeDrop(i2, fragPx); outCol = mix(outCol, s, s.a); cov = max(cov, s.a); }
    if (i1.x < 0.05) { half4 s = shadeDrop(i1, fragPx); outCol = mix(outCol, s, s.a); cov = max(cov, s.a); }

    float sx = fragPx.x / 44.0;
    float col1 = floor(sx);
    float h1 = hash12(float2(col1, 91.7));
    float streak = 0.0;
    if (h1 < 0.4) {
        float y0 = fract(fragPx.y / 1610.0 * (0.5 + h1) + tt * (0.30 + 0.35 * h1) + h1 * 13.0);
        float m = smoothstep(0.0, 0.45, y0) * (1.0 - smoothstep(0.55, 1.0, y0));
        float inCol2 = smoothstep(0.0, 0.38, fract(sx)) * (1.0 - smoothstep(0.62, 1.0, fract(sx)));
        streak = m * inCol2 * 0.07;
    }

    outCol.rgb += half3(streak);
    return half4(outCol.rgb, base.a);
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