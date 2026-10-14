in vec2 vTexCoord;
in vec2 vPosition;
in vec4 vColor;

uniform vec2 u_GradientStartPos;
uniform vec2 u_GradientEndPos;
uniform vec4 u_GradientStart;
uniform vec4 u_GradientEnd;

uniform sampler2D tex;
uniform int hasTexture;

uniform vec4 u_GradientCanvas;

vec2 snappedPosition() {
    return floor(vPosition * 256.0 + 0.5) / 256.0;
}

void main() {
    vec2 normalizedPos;
    if (u_GradientCanvas.z > u_GradientCanvas.x && u_GradientCanvas.w > u_GradientCanvas.y) {
        vec2 rectSize = vec2(u_GradientCanvas.z - u_GradientCanvas.x, u_GradientCanvas.w - u_GradientCanvas.y);
        normalizedPos = (snappedPosition() - u_GradientCanvas.xy) / rectSize;
    } else {
        normalizedPos = snappedPosition();
    }

    vec2 direction = u_GradientEndPos - u_GradientStartPos;
    float t = 0.0;

    if (length(direction) > 0.001) {
        t = dot(normalizedPos - u_GradientStartPos, direction) / dot(direction, direction);
        t = clamp(t, 0.0, 1.0);
    }

    vec4 gradientColor = mix(u_GradientStart, u_GradientEnd, t);

    vec4 finalColor;
    if (hasTexture == 1) {
        vec4 texColor = texture(tex, vTexCoord);
        finalColor = vec4(texColor.rgb * gradientColor.rgb, texColor.a * gradientColor.a);
    } else {
        finalColor = gradientColor;
    }

    fragColor = finalColor * vColor;
}