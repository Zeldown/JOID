#version 120

varying vec2 pos;

uniform vec2 startPos;
uniform vec2 endPos;
uniform vec4 startColor;
uniform vec4 endColor;

uniform vec4 canvas;

void main() {
    vec2 uv = (pos.xy - canvas.xy) / canvas.zw;
    vec2 direction = endPos - startPos;

    float t = dot(uv - startPos, direction) / dot(direction, direction);
    t = clamp(t, 0.0, 1.0);

    vec4 color = mix(startColor, endColor, t);
    gl_FragColor = vec4(color.r, color.g, color.b, color.a);
}