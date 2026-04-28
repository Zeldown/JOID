#version 120

varying vec2 vTexCoord;
varying vec4 vColor;
varying vec2 vPosition;

uniform sampler2D msdf;

uniform float doffset;
uniform float blend;
uniform vec4 color;
uniform vec2 texel;

uniform int u_HasGradient;
uniform vec4 u_GradientStart;
uniform vec4 u_GradientEnd;
uniform vec2 u_GradientStartPos;
uniform vec2 u_GradientEndPos;
uniform vec4 u_GradientCanvas;

uniform int effectType;
uniform vec4 effectColor;
uniform float effectSize;
uniform vec2 shadowOffset;

float smoother(float edge0, float edge1, float x) {
    x = clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
    return x * x * (3.0 - 2.0 * x);
}

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

float sdfDistance(sampler2D msdf, vec2 pos) {
    vec3 raw = texture2D(msdf, pos).rgb;
    return max(min(raw.r, raw.g), min(max(raw.r, raw.g), raw.b));
}

vec4 resolveColor() {
    if (u_HasGradient == 0) {
        return color;
    }

    vec2 normalizedPos;
    if (u_GradientCanvas.z > u_GradientCanvas.x && u_GradientCanvas.w > u_GradientCanvas.y) {
        vec2 rectSize = vec2(u_GradientCanvas.z - u_GradientCanvas.x, u_GradientCanvas.w - u_GradientCanvas.y);
        normalizedPos = (vPosition - u_GradientCanvas.xy) / rectSize;
    } else {
        normalizedPos = vPosition;
    }

    vec2 dir = u_GradientEndPos - u_GradientStartPos;
    float t = 0.0;
    if (length(dir) > 0.001) {
        t = dot(normalizedPos - u_GradientStartPos, dir) / dot(dir, dir);
        t = clamp(t, 0.0, 1.0);
    }

    return mix(u_GradientStart, u_GradientEnd, t);
}

void main() {
    float distance = sdfDistance(msdf, vTexCoord);

   	vec2 grad = vec2(
        sdfDistance(msdf, vTexCoord + vec2(texel.x, 0.0)) - distance,
        sdfDistance(msdf, vTexCoord + vec2(0.0, texel.y)) - distance
    );

    float grad_length = length(grad);

    grad /= max(grad_length, 1.0 / 256.0);

    float vgrad = abs(grad.y);
    float res_doffset = mix(doffset, mix(doffset * 1.1, doffset * 0.8, vgrad), blend);
    float alpha = smoother(0.5 - res_doffset, 0.5 + res_doffset, distance);

    vec4 baseColor = resolveColor();
    vec4 finalColor = vec4(baseColor.rgb, alpha * baseColor.a);

    if (finalColor.a < 10.0 / 256.0) {
        gl_FragColor = vec4(0.0);
    } else {
        gl_FragColor = finalColor * vColor;
    }
}