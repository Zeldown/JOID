#version 120

varying vec2 vTexCoord;
varying vec2 vPosition;
varying vec4 vColor;

uniform float radius;
uniform vec2 center;

uniform sampler2D texture;

void main() {
    float dist = length(vPosition - center);
    
    vec4 finalColor;
    vec4 textureColor = texture2D(texture, vTexCoord);
    if (textureColor.a < 0.01 || (textureColor.r + textureColor.g + textureColor.b) < 0.01) {
        finalColor = vColor;
    } else {
        finalColor = textureColor;
    }
    
    float alpha = 1.0 - smoothstep(radius - 1.0, radius, dist);
    finalColor.a *= alpha;
    gl_FragColor = finalColor;
}