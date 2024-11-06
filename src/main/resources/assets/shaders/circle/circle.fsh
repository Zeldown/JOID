#version 110

varying vec2 pos;

uniform float radius;
uniform vec2 center;
uniform sampler2D texture;

void main() {
	vec4 canvas = vec4(center.x - radius, center.y - radius, center.x + radius, center.y + radius);
    vec2 uv = (pos.xy - canvas.xy) / canvas.zw;
    vec4 textureColor = texture2D(texture, uv);
    
    float v = length(pos - center);
    float a = 1.0 - smoothstep(radius - 1.0, radius, v);
    
    vec4 color = textureColor.rgb == vec3(0.0) ? gl_Color : textureColor;
    gl_FragColor = color * vec4(1.0, 1.0, 1.0, a);
}