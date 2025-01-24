#version 120

varying vec2 pos;
varying vec2 uv;

void main() {
    pos = gl_Vertex.xy;
    uv = vec2(gl_MultiTexCoord0);

    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    gl_FrontColor = gl_Color;
}