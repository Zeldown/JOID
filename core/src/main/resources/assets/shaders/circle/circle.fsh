in vec2 vTexCoord;
in vec2 vPosition;
in vec4 vColor;

uniform float radius;
uniform vec2 center;
uniform int type;

uniform int gradient;
uniform vec2 startPos;
uniform vec2 endPos;
uniform vec4 startColor;
uniform vec4 endColor;
uniform vec4 canvas;

uniform sampler2D tex;

vec2 snappedPosition() {
    return floor(vPosition * 256.0 + 0.5) / 256.0;
}

vec4 gradientColor() {
    vec2 normalizedPos = snappedPosition();
    if (canvas.z > canvas.x && canvas.w > canvas.y) {
        normalizedPos = (snappedPosition() - canvas.xy) / (canvas.zw - canvas.xy);
    }

    vec2 direction = endPos - startPos;
    float t = 0.0;
    if (length(direction) > 0.001) {
        t = clamp(dot(normalizedPos - startPos, direction) / dot(direction, direction), 0.0, 1.0);
    }

    return mix(startColor, endColor, t);
}

void main() {
    float dist = length(snappedPosition() - center);

    vec4 baseColor = texture(tex, vTexCoord) * vColor;
    if (type == 1) {
        baseColor = texture(tex, vTexCoord);
    } else if (type == 2) {
        baseColor = vColor;
    }

    if (gradient == 1) {
        baseColor *= gradientColor();
    }

    float mask = 1.0 - smoothstep(radius - 1.0, radius, dist);

    if (type == 1) {
        fragColor = vec4(baseColor.rgb * mask, baseColor.a * mask);
    } else {
        fragColor = vec4(baseColor.rgb, baseColor.a * mask);
    }
}