#version 120

varying vec2 pos;

uniform float radius;
uniform vec4 canvas;
uniform sampler2D texture;

void main() {
    vec2 uv = (pos.xy - canvas.xy) / canvas.zw;
    vec4 textureColor = texture2D(texture, uv);
    
    float pi = 6.28318530718;
    
    float directions = 64.0;
    float quality = 32.0;
   
    vec2 mappedRadius = radius / canvas.zw;
    for (float d = 0.0; d < pi; d += pi / directions) {
		for (float i = 1.0 / quality; i <= 1.0; i += 1.0 / quality) {
			textureColor += texture2D(texture, uv + vec2(cos(d), sin(d)) * mappedRadius * i);		
        }
    }
    
    textureColor /= quality * directions - 15.0;
    gl_FragColor = textureColor;
}