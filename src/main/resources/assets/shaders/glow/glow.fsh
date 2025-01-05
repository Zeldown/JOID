#version 120

varying vec2 pos;

uniform float radius;
uniform vec2 center;
uniform vec4 color;

void main() {    
    float length = length(pos - center);    
	float alpha = 1.0 - (length / radius);
	alpha = max(0.0, min(1.0, alpha)) * color.a;
    gl_FragColor = vec4(color.r, color.g, color.b, alpha);
}