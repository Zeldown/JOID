#version 450

layout(location = 0) in vec2 vPosition;
layout(location = 1) in vec2 vTexCoord;
layout(location = 2) in vec4 vColor;

layout(std140, binding = 1) uniform FragmentUniforms {
    float u_Radius;
    vec4 u_InnerRect;
    int u_Type;
};

layout(binding = 2) uniform sampler2D tex;

layout(location = 0) out vec4 fragColor;

void main() {
    vec2 tl = u_InnerRect.xy - vPosition;
    vec2 br = vPosition - u_InnerRect.zw;
    vec2 distances = max(br, tl);

    float distanceToCorner = length(max(vec2(0.0), distances)) - u_Radius;

    vec4 baseColor = texture(tex, vTexCoord) * vColor;
    if (u_Type == 1) {
        baseColor = texture(tex, vTexCoord);
    } else if (u_Type == 2) {
        baseColor = vColor;
    }

    float mask = 1.0 - smoothstep(0.0, 1.0, distanceToCorner);

    if (u_Type == 1) {
        fragColor = vec4(baseColor.rgb * mask, baseColor.a * mask);
    } else {
        fragColor = vec4(baseColor.rgb, baseColor.a * mask);
    }
}