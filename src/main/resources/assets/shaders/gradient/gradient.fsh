#version 120

varying vec2 pos;

uniform vec2 startPos;
uniform vec2 endPos;
uniform vec4 startColor;
uniform vec4 endColor;

uniform vec4 canvas;
uniform sampler2D texture;

void main() {
    vec2 uv = (pos.xy - canvas.xy) / canvas.zw;
    vec2 direction = endPos - startPos;

    float t = dot(uv - startPos, direction) / dot(direction, direction);
    t = clamp(t, 0.0, 1.0);

    vec4 color = mix(startColor, endColor, t);
    vec4 textureColor = texture2D(texture, uv);
    
    gl_FragColor = vec4(color.rgb + textureColor.rgb, color.a);
}