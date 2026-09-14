#version 330 core

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec2 aTexCoord;
layout(location = 2) in vec4 aColor;
layout(location = 3) in vec3 aNormal;

uniform mat4 uProjectionMatrix;
uniform mat4 uModelViewMatrix;
uniform mat3 uNormalMatrix;
uniform bool uLighting;

out vec2 vTexCoord;
out vec4 vColor;
flat out vec4 vLitColor;

void main() {
    vTexCoord = aTexCoord;
    vColor = aColor;

    float diffuse = max(dot(uNormalMatrix * aNormal, vec3(0.0, 0.0, 1.0)), 0.0);
    vLitColor = vec4(clamp(aColor.rgb * (0.6 + diffuse), 0.0, 1.0), aColor.a);

    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);
}