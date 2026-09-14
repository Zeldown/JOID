#version 450

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec2 aTexCoord;
layout(location = 2) in vec4 aColor;
layout(location = 3) in vec4 aNormal;

layout(std140, binding = 0) uniform VertexUniforms {
    mat4 uProjectionMatrix;
    mat4 uModelViewMatrix;
    mat3 uNormalMatrix;
    int uLighting;
};

layout(location = 1) out vec2 vTexCoord;
layout(location = 2) out vec4 vColor;
layout(location = 3) flat out vec4 vLitColor;
layout(location = 4) flat out int vLighting;

void main() {
    vTexCoord = aTexCoord;
    vColor = aColor;
    vLighting = uLighting;

    float diffuse = max(dot(uNormalMatrix * aNormal.xyz, vec3(0.0, 0.0, 1.0)), 0.0);
    vLitColor = vec4(clamp(aColor.rgb * (0.6 + diffuse), 0.0, 1.0), aColor.a);

    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);
}