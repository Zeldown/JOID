#version 330 core

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec2 aTexCoord;
layout(location = 2) in vec4 aColor;

uniform mat4 uProjectionMatrix;
uniform mat4 uModelViewMatrix;

out vec2 vTexCoord;
out vec4 vColor;
out vec2 vPosition;

void main() {
    vTexCoord = aTexCoord;
    vPosition = aPosition.xy;
    vColor = aColor;

    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);
}