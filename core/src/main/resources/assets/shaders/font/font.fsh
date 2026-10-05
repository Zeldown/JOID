in vec2 vTexCoord;
in vec4 vColor;
in vec2 vPosition;

uniform sampler2D msdf;
uniform vec4 color;
uniform vec2 texel;
uniform vec2 pixel;
uniform float pxRange;

uniform int u_HasGradient;
uniform vec4 u_GradientStart;
uniform vec4 u_GradientEnd;
uniform vec2 u_GradientStartPos;
uniform vec2 u_GradientEndPos;
uniform vec4 u_GradientCanvas;

vec2 snappedPosition() {
    return floor(vPosition * 256.0 + 0.5) / 256.0;
}

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

float sdfDistance(vec2 pos) {
    vec3 raw = texture(msdf, pos).rgb;
    return median(raw.r, raw.g, raw.b);
}

float screenPxRange() {
    vec2 unitRange = vec2(pxRange) * texel;
    vec2 screenTexSize = vec2(1.0) / pixel;
    return max(0.5 * dot(unitRange, screenTexSize), 1.0);
}

float msdfAlpha(vec2 uv, float pxR) {
    float d = sdfDistance(uv);
    return clamp(pxR * (d - 0.5) + 0.5, 0.0, 1.0);
}

float supersampledAlpha() {
    float pxR = 2.0 * screenPxRange();
    vec2 dx = vec2(pixel.x * 0.25, 0.0);
    vec2 dy = vec2(0.0, pixel.y * 0.25);
    float a = msdfAlpha(vTexCoord + dx + dy, pxR);
    a += msdfAlpha(vTexCoord - dx + dy, pxR);
    a += msdfAlpha(vTexCoord + dx - dy, pxR);
    a += msdfAlpha(vTexCoord - dx - dy, pxR);
    return a / 4.0;
}

vec4 resolveColor() {
    if (u_HasGradient == 0) {
        return color;
    }

    vec2 normalizedPos;
    if (u_GradientCanvas.z > u_GradientCanvas.x && u_GradientCanvas.w > u_GradientCanvas.y) {
        vec2 rectSize = vec2(u_GradientCanvas.z - u_GradientCanvas.x, u_GradientCanvas.w - u_GradientCanvas.y);
        normalizedPos = (snappedPosition() - u_GradientCanvas.xy) / rectSize;
    } else {
        normalizedPos = snappedPosition();
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