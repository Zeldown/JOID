in vec2 vPosition;
in vec2 vTexCoord;
in vec4 vColor;

uniform float u_Radius;
uniform vec4 u_InnerRect;
uniform int u_Type;
uniform float u_Stroke;

uniform int u_Gradient;
uniform vec2 u_StartPos;
uniform vec2 u_EndPos;
uniform vec4 u_StartColor;
uniform vec4 u_EndColor;
uniform vec4 u_Canvas;

uniform sampler2D tex;

vec2 snappedPosition() {
    return floor(vPosition * 256.0 + 0.5) / 256.0;
}

vec4 gradientColor() {
    vec2 normalizedPos = snappedPosition();
    if (u_Canvas.z > u_Canvas.x && u_Canvas.w > u_Canvas.y) {
        normalizedPos = (snappedPosition() - u_Canvas.xy) / (u_Canvas.zw - u_Canvas.xy);
    }

    vec2 direction = u_EndPos - u_StartPos;
    float t = 0.0;
    if (length(direction) > 0.001) {
        t = clamp(dot(normalizedPos - u_StartPos, direction) / dot(direction, direction), 0.0, 1.0);
    }

    return mix(u_StartColor, u_EndColor, t);
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

    if (u_Gradient == 1) {
        baseColor *= gradientColor();
    }

    float mask = 1.0 - smoothstep(0.0, 1.0, distanceToCorner);
    if (u_Stroke > 0.0) {
        mask *= smoothstep(-0.5, 0.5, distanceToCorner + u_Stroke);
    }

    if (u_Type == 1) {
        fragColor = vec4(baseColor.rgb * mask, baseColor.a * mask);
    } else {
        fragColor = vec4(baseColor.rgb, baseColor.a * mask);
    }
}