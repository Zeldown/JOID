in vec2 vTexCoord;
in vec2 vPosition;
in vec4 vColor;

uniform vec2 startPos;
uniform vec2 endPos;
uniform vec4 startColor;
uniform vec4 endColor;

uniform sampler2D tex;
uniform int hasTexture;

uniform vec4 canvas;

vec2 snappedPosition() {
    return floor(vPosition * 256.0 + 0.5) / 256.0;
}

void main() {
    vec2 normalizedPos;
    if (canvas.z > canvas.x && canvas.w > canvas.y) {
        vec2 rectSize = vec2(canvas.z - canvas.x, canvas.w - canvas.y);
        normalizedPos = (snappedPosition() - canvas.xy) / rectSize;
    } else {
        normalizedPos = snappedPosition();
    }

    vec2 direction = endPos - startPos;
    float t = 0.0;

    if (length(direction) > 0.001) {
        t = dot(normalizedPos - startPos, direction) / dot(direction, direction);
        t = clamp(t, 0.0, 1.0);
    }

    vec4 gradientColor = mix(startColor, endColor, t);

    vec4 finalColor;
    if (hasTexture == 1) {
        vec4 texColor = texture(tex, vTexCoord);
        finalColor = vec4(texColor.rgb * gradientColor.rgb, texColor.a * gradientColor.a);
    } else {
        finalColor = gradientColor;
    }

    fragColor = finalColor * vColor;
}