#version 110

varying vec2 f_Position;
varying vec2 TexCoord;

void main() {
    f_Position = gl_Vertex.xy;

    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    gl_FrontColor = gl_Color;
    TexCoord = vec2(gl_MultiTexCoord0);
}