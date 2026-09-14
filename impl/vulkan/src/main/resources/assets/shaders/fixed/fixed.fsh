#version 450

layout(location = 1) in vec2 vTexCoord;
layout(location = 2) in vec4 vColor;
layout(location = 3) flat in vec4 vLitColor;
layout(location = 4) flat in int vLighting;

layout(binding = 2) uniform sampler2D tex;

layout(location = 0) out vec4 fragColor;

void main() {
    fragColor = texture(tex, vTexCoord) * (vLighting != 0 ? vLitColor : vColor);
}