#version 450

layout(location = 1) in vec2 vTexCoord;
layout(location = 2) in vec4 vColor;
layout(location = 0) in vec2 vPosition;

layout(binding = 2) uniform sampler2D msdf;
layout(std140, binding = 1) uniform FragmentUniforms {
    vec4 color;
    vec2 texel;
    float pxRange;
    int u_HasGradient;
    vec4 u_GradientStart;
    vec4 u_GradientEnd;
    vec2 u_GradientStartPos;
    vec2 u_GradientEndPos;
    vec4 u_GradientCanvas;
};

layout(location = 0) out vec4 fragColor;

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

float sdfDistance(vec2 pos) {
    vec3 raw = texture(msdf, pos).rgb;
    return median(raw.r, raw.g, raw.b);
}

float screenPxRange() {
    vec2 unitRange = vec2(pxRange) * texel;
    vec2 screenTexSize = vec2(1.0) / fwidth(vTexCoord);
    return max(0.5 * dot(unitRange, screenTexSize), 1.0);
}

float msdfAlpha(vec2 uv, float pxR) {
    float d = sdfDistance(uv);
    return clamp(pxR * (d - 0.5) + 0.5, 0.0, 1.0);
}

float supersampledAlpha() {
    float pxR = screenPxRange();
    vec2 maxOffset = texel * 0.5;
    vec2 dx = clamp(dFdx(vTexCoord) * 0.354, -maxOffset, maxOffset);
    vec2 dy = clamp(dFdy(vTexCoord) * 0.354, -maxOffset, maxOffset);
    float a = msdfAlpha(vTexCoord, pxR);
    a += msdfAlpha(vTexCoord + dx + dy, pxR);
    a += msdfAlpha(vTexCoord - dx + dy, pxR);
    a += msdfAlpha(vTexCoord + dx - dy, pxR);
    a += msdfAlpha(vTexCoord - dx - dy, pxR);
    return a / 5.0;
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
    float alpha = supersampledAlpha();

    if (alpha < 1.0 / 256.0) {
        discard;
    }

    vec4 baseColor = resolveColor();
    fragColor = vec4(baseColor.rgb, alpha * baseColor.a) * vColor;
}