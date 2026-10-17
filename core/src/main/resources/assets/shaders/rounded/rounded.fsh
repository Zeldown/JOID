in vec2 vPosition;
in vec2 vTexCoord;
in vec4 vColor;

uniform float u_Radius;
uniform vec4 u_InnerRect;
uniform int u_Type;
uniform float u_Stroke;
uniform int u_Aligned;
uniform float u_Pixel;

uniform int u_HasGradient;
uniform vec2 u_GradientStartPos;
uniform vec2 u_GradientEndPos;
uniform vec4 u_GradientStart;
uniform vec4 u_GradientEnd;
uniform vec4 u_GradientCanvas;

uniform sampler2D tex;

vec2 snappedPosition() {
    if (u_Aligned == 0) {
        return vPosition;
    }

    return floor(vPosition * 256.0 + 0.5) / 256.0;
}

vec4 gradientColor() {
    vec2 normalizedPos = snappedPosition();
    if (u_GradientCanvas.z > u_GradientCanvas.x && u_GradientCanvas.w > u_GradientCanvas.y) {
        normalizedPos = (snappedPosition() - u_GradientCanvas.xy) / (u_GradientCanvas.zw - u_GradientCanvas.xy);
    }

    vec2 direction = u_GradientEndPos - u_GradientStartPos;
    float t = 0.0;
    if (length(direction) > 0.001) {
        t = clamp(dot(normalizedPos - u_GradientStartPos, direction) / dot(direction, direction), 0.0, 1.0);
    }

    return mix(u_GradientStart, u_GradientEnd, t);
}

void main() {
    vec2 tl = u_InnerRect.xy - snappedPosition();
    vec2 br = snappedPosition() - u_InnerRect.zw;
    vec2 distances = max(br, tl);

    float distanceToCorner = length(max(vec2(0.0), distances)) + min(max(distances.x, distances.y), 0.0) - u_Radius;

    vec4 baseColor = texture(tex, vTexCoord) * vColor;
    if (u_Type == 1) {
        baseColor = texture(tex, vTexCoord);
    } else if (u_Type == 2) {
        baseColor = vColor;
    }

    if (u_HasGradient == 1) {
        baseColor *= gradientColor();
    }

    float mask = 1.0 - smoothstep(0.0, u_Pixel, distanceToCorner);
    if (u_Aligned == 0) {
        mask = 1.0 - clamp(distanceToCorner / u_Pixel, 0.0, 1.0);
    }

    if (u_Stroke > 0.0) {
        mask *= u_Aligned == 0 ? clamp((distanceToCorner + u_Stroke) / u_Pixel + 0.5, 0.0, 1.0) : smoothstep(-0.5 * u_Pixel, 0.5 * u_Pixel, distanceToCorner + u_Stroke);
    }

    if (u_Type == 1) {
        fragColor = vec4(baseColor.rgb * mask, baseColor.a * mask);
    } else {
        fragColor = vec4(baseColor.rgb, baseColor.a * mask);
    }
}