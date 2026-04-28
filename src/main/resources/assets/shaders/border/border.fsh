#version 120

varying vec2 vTexCoord;
varying vec2 vPosition;
varying vec4 vColor;

uniform sampler2D tex;
uniform float u_BorderWidth;
uniform vec4 u_BorderColor;
uniform vec2 u_TexelSize;
uniform int u_Fill;
uniform int u_Mode;
uniform vec4 u_Rect;

uniform int u_HasGradient;
uniform vec4 u_GradientStart;
uniform vec4 u_GradientEnd;
uniform vec2 u_GradientStartPos;
uniform vec2 u_GradientEndPos;
uniform vec4 u_GradientCanvas;

vec4 computeBorderColor() {
    if (u_HasGradient == 0) {
        return u_BorderColor;
    }

    vec2 normalizedPos;
    if (u_GradientCanvas.z > u_GradientCanvas.x && u_GradientCanvas.w > u_GradientCanvas.y) {
        vec2 rectSize = vec2(u_GradientCanvas.z - u_GradientCanvas.x, u_GradientCanvas.w - u_GradientCanvas.y);
        normalizedPos = (vPosition - u_GradientCanvas.xy) / rectSize;
    } else {
        normalizedPos = vPosition;
    }

    vec2 direction = u_GradientEndPos - u_GradientStartPos;
    float t = 0.0;
    if (length(direction) > 0.001) {
        t = dot(normalizedPos - u_GradientStartPos, direction) / dot(direction, direction);
        t = clamp(t, 0.0, 1.0);
    }

    return mix(u_GradientStart, u_GradientEnd, t);
}

void main() {
    vec4 original = texture2D(tex, vTexCoord);

    if (u_Fill == 0) {
        bool outsideX = vPosition.x < u_Rect.x || vPosition.x > u_Rect.z;
        bool outsideY = vPosition.y < u_Rect.y || vPosition.y > u_Rect.w;
        if (outsideX && outsideY) {
            gl_FragColor = original;
            return;
        }
    }

    vec4 borderColor = computeBorderColor();

    vec3 originalStraight = original.rgb / max(original.a, 0.001);

    if (u_Mode == 1) {
        float minAlpha = original.a;

        for (int i = 0; i < 24; i++) {
            float angle = float(i) * 0.261799;
            vec2 dir = vec2(cos(angle), sin(angle));
            minAlpha = min(minAlpha, texture2D(tex, vTexCoord + dir * u_BorderWidth * u_TexelSize).a);
            minAlpha = min(minAlpha, texture2D(tex, vTexCoord + dir * u_BorderWidth * 0.66 * u_TexelSize).a);
            minAlpha = min(minAlpha, texture2D(tex, vTexCoord + dir * u_BorderWidth * 0.33 * u_TexelSize).a);
        }

        float reference = max(original.a, 0.001);
        float insideRect = smoothstep(0.0, 0.01, original.a);
        float touchesEdge = smoothstep(0.0, 1.0, (original.a - minAlpha) / reference);
        float borderMask = insideRect * touchesEdge;
        gl_FragColor = vec4(mix(originalStraight, borderColor.rgb, borderMask * borderColor.a), original.a);
    } else {
        float maxAlpha = original.a;
        float refAlpha = original.a;

        for (int i = 0; i < 24; i++) {
            float angle = float(i) * 0.261799;
            vec2 dir = vec2(cos(angle), sin(angle));
            maxAlpha = max(maxAlpha, texture2D(tex, vTexCoord + dir * u_BorderWidth * u_TexelSize).a);
            maxAlpha = max(maxAlpha, texture2D(tex, vTexCoord + dir * u_BorderWidth * 0.66 * u_TexelSize).a);
            maxAlpha = max(maxAlpha, texture2D(tex, vTexCoord + dir * u_BorderWidth * 0.33 * u_TexelSize).a);
            refAlpha = max(refAlpha, texture2D(tex, vTexCoord + dir * u_BorderWidth * 1.5 * u_TexelSize).a);
            refAlpha = max(refAlpha, texture2D(tex, vTexCoord + dir * u_BorderWidth * 2.0 * u_TexelSize).a);
        }
        refAlpha = max(refAlpha, maxAlpha);

        float reference = max(maxAlpha, 0.001);
        float haloFactor = smoothstep(0.0, 1.0, (maxAlpha - original.a) / reference);
        float outerFade = refAlpha > 0.001 ? smoothstep(0.0, refAlpha, maxAlpha) : 0.0;
        float borderAlpha = borderColor.a * haloFactor * outerFade;
        float outAlpha = original.a + borderAlpha * (1.0 - original.a);
        vec3 outRGB = (original.rgb + borderColor.rgb * borderAlpha * (1.0 - original.a)) / max(outAlpha, 0.001);

        gl_FragColor = vec4(outRGB, outAlpha);
    }
}
