#version 450

layout(location = 1) in vec2 vTexCoord;
layout(location = 0) in vec2 vPosition;
layout(location = 2) in vec4 vColor;

layout(std140, binding = 1) uniform FragmentUniforms {
    float radius;
    vec2 center;
    int type;
};

layout(binding = 2) uniform sampler2D tex;

layout(location = 0) out vec4 fragColor;

void main() {
    float dist = length(vPosition - center);

    vec4 baseColor = texture(tex, vTexCoord) * vColor;
    if (type == 1) {
        baseColor = texture(tex, vTexCoord);
    } else if (type == 2) {
        baseColor = vColor;
    }

    float mask = 1.0 - smoothstep(radius - 1.0, radius, dist);

    if (type == 1) {
        fragColor = vec4(baseColor.rgb * mask, baseColor.a * mask);
    } else {
        fragColor = vec4(baseColor.rgb, baseColor.a * mask);
    }
}