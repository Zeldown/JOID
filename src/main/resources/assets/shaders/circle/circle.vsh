#version 120

uniform mat4 uModelMatrix;
uniform mat4 uViewMatrix;
uniform mat4 uProjectionMatrix;

varying vec2 vTexCoord;
varying vec2 vPosition;
varying vec4 vColor;

void main() {
    vTexCoord = gl_MultiTexCoord0.xy;
    vPosition = gl_Vertex.xy;
    vColor = gl_Color;
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}