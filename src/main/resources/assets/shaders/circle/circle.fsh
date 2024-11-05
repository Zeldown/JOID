#version 110

varying vec2 pos;

uniform float radius;
uniform vec2 center;

void main() {
    float v = length(pos - center);
    float a = 1.0 - smoothstep(radius - 1.0, radius, v);
    
    vec4 color = textureColor.rgb == vec3(0.0) ? gl_Color : textureColor;
    gl_FragColor = color * vec4(1.0, 1.0, 1.0, a);
}