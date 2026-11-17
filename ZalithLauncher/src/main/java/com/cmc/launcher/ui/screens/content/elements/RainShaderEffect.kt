package com.cmc.launcher.ui.screens.content.elements

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

float sdDrop(float2 p, float r) {
    float d = length(p) - r;
    d -= 0.18 * r * smoothstep(-0.4 * r, 0.9 * r, -p.y);
    d += 0.10 * r * smoothstep(0.2 * r, r, p.y) * (1.0 - smoothstep(r, 1.6 * r, p.y));
    return d;
}

float dropLayer(float2 uv, float cellPx, float speed, float minR, float maxR, float tt, float grow) {
    float2 st = uv * (resolution.y / cellPx);
    st.y += tt * speed;
    float2 id = floor(st);
    float2 gv = fract(st) - 0.5;

    float2 rnd = hash22(id);
    float2 off = (rnd - 0.5) * 0.72;
    float2 p = gv - off;

    float phase = fract(rnd.x * 7.0 + tt * (0.12 + 0.2 * rnd.y));
    float life = smoothstep(0.0, 0.08, phase) * (1.0 - smoothstep(0.86, 1.0, phase));
    if (life <= 0.0) return 1.0;

    float r = mix(minR, maxR, rnd.y) * (0.6 + 0.4 * life) * (1.0 + grow * (st.y - tt * speed) * 0.0);

    float d = sdDrop(p, r);
    float drop = smoothstep(0.02, -0.05, d);
    return drop * life;
}

float heightField(float2 fragPx, float tt) {
    float asp = resolution.x / resolution.y;
    float2 uv = fragPx / resolution.y;
    float h = 0.0;

    h += (1.0 - dropLayer(uv, 54.0, 0.0, 0.13, 0.30, tt * 0.5, 0.0)) * 0.55;
    h += (1.0 - dropLayer(uv, 86.0, 0.14, 0.20, 0.42, tt, 0.0)) * 0.85;
    h += (1.0 - dropLayer(uv, 140.0, 0.55, 0.26, 0.55, tt * 1.3, 0.0)) * 1.0;

    return h;
}

float streakField(float2 fragPx, float tt) {
    float2 uv = fragPx / resolution.y;
    float s = 0.0;
    for (int k = 0; k < 3; k++) {
        float fk = float(k);
        float speed = 5.0 + fk * 3.5;
        float scaleX = 34.0 + fk * 26.0;
        float x = uv.x * scaleX;
        float col = floor(x);
        float h = hash12(float2(col, fk * 91.7));
        float y0 = fract(uv.y * (0.6 + h * 0.8) + tt * speed * (0.7 + h * 0.6) + h * 17.0);
        float lineH = 0.045 + h * 0.035;
        float m = smoothstep(0.0, 0.25, y0) * (1.0 - smoothstep(0.75, 1.0, y0));
        float inCol = smoothstep(0.0, 0.22, fract(x)) * (1.0 - smoothstep(0.78, 1.0, fract(x)));
        s += m * inCol * 0.16 * (1.0 - fk * 0.22);
    }
    return s;
}

half4 main(float2 fragCoord) {
    float tt = t;
    float e = 1.25;
    float hC = heightField(fragCoord, tt);
    float hR = heightField(fragCoord + float2(e, 0.0), tt);
    float hU = heightField(fragCoord + float2(0.0, e), tt);

    float2 n = normalize(float2(hR - hC, hU - hC) + 1e-5);
    if (hC <= 0.001) n = float2(0.0);

    float2 refractOff = n * hC * 22.0 * float2(1.0, -1.0);
    half4 bg = content.eval(fragCoord + refractOff);

    float fres = pow(1.0 - clamp(hC * 0.9, 0.0, 1.0), 3.0);
    float rim = fres * (0.35 + 0.65 * hC) * 0.85;

    float2 lightDir = normalize(float2(-0.62, -0.78));
    float spec = pow(max(dot(n, lightDir), 0.0), 34.0) * min(hC * 1.6, 1.0);

    float streak = streakField(fragCoord, tt);

    half3 col = bg.rgb;
    col += half3(spec * 1.35);
    col += half3(rim * 0.5);
    col = mix(col, half3(0.72, 0.83, 0.92) * 1.06 + bg.rgb * 0.28, clamp(rim * 0.55, 0.0, 0.75));
    col += half3(streak);

    float vign = smoothstep(1.6, 0.35, length((fragCoord / resolution - 0.5) * 1.25));
    col *= half3(0.97 + 0.03 * vign);

    return half4(col, 1.0);
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