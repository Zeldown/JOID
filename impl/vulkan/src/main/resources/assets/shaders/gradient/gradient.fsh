#version 450

layout(location = 1) in vec2 vTexCoord;
layout(location = 0) in vec2 vPosition;
layout(location = 2) in vec4 vColor;

layout(std140, binding = 1) uniform FragmentUniforms {
    vec2 startPos;
    vec2 endPos;
    vec4 startColor;
    vec4 endColor;
    int hasTexture;
    vec4 canvas;
};

layout(binding = 2) uniform sampler2D tex;

layout(location = 0) out vec4 fragColor;

void main() {
    vec2 normalizedPos;
    if (canvas.z > canvas.x && canvas.w > canvas.y) {
        vec2 rectSize = vec2(canvas.z - canvas.x, canvas.w - canvas.y);
        normalizedPos = (vPosition - canvas.xy) / rectSize;
    } else {
        normalizedPos = vPosition;
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