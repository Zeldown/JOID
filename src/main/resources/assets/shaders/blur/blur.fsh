#version 120

varying vec2 pos;

uniform float radius;
uniform vec4 canvas;
uniform sampler2D texture;

void main() {
    vec2 uv = (pos.xy - canvas.xy) / canvas.zw;
    vec4 textureColor = texture2D(texture, uv);
    
    vec4 color = vec4(0.0);
    vec2 texOffset = 1.0 / canvas.zw;
    
    float total = 0.0;
    for (float x = -radius; x <= radius; x++) {
        for (float y = -radius; y <= radius; y++) {
            float weight = exp(-(x * x + y * y) / (2.0 * radius * radius));
            color += texture2D(texture, uv + vec2(x, y) * texOffset) * weight;
            total += weight;
        }
    }

    gl_FragColor = color / total;
}