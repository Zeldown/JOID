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

    vec4 mixedColor = mix(startColor, endColor, t);
    vec4 textureColor = texture2D(texture, uv);
    
    vec4 color = textureColor.rgb == vec3(0.0) ? gl_Color : textureColor;
    gl_FragColor = vec4(mixedColor.rgb * color.rgb, mixedColor.a);
}