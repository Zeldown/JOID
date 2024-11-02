#version 120

varying vec2 pos;

uniform float radius;
uniform vec4 canvas;
uniform sampler2D texture;

void main() {
    vec2 uv = (pos.xy - (canvas.xy - vec2(radius))) / (canvas.zw + vec2(radius));
    vec4 textureColor = texture2D(texture, uv);

    vec2 tl = canvas.xy - pos;
    vec2 br = pos - canvas.zw;
    vec2 dis = max(br, tl);

    float t = length(max(vec2(0.0, 0.0), dis)) - radius;
    float a = 1.0 - smoothstep(0.0, 1.0, t);
    
    vec4 color = textureColor.rgb == vec3(0.0) ? gl_Color : textureColor;
    gl_FragColor = color * vec4(1.0, 1.0, 1.0, min(a, (textureColor.a, gl_Color.a)));
}