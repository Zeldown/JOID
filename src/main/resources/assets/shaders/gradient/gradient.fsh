#version 120

varying vec2 vTexCoord;
varying vec2 vPosition;
varying vec4 vColor;

uniform vec2 startPos;
uniform vec2 endPos;
uniform vec4 startColor;
uniform vec4 endColor;

uniform sampler2D texture;
uniform int hasTexture;

uniform vec4 canvas;

void main() {
    vec2 normalizedPos;
    if (canvas.z > 0.0 && canvas.w > 0.0) {
        normalizedPos = (vPosition - canvas.xy) / canvas.zw;
    } else {
        normalizedPos = vPosition;
    }
    
    vec2 direction = endPos - startPos;
    float t = 0.0;
    
    if (length(direction) > 0.001) {
        t = dot(normalizedPos - startPos, direction) / dot(direction, direction);
        t = clamp(t, 0.0, 1.0);
    }
    
    vec4 gradientColor = mix(startColor, endColor, t);
    
    vec4 finalColor;
    if (hasTexture == 1) {
        vec4 texColor = texture2D(texture, vTexCoord);
        finalColor = vec4(texColor.rgb * gradientColor.rgb, texColor.a * gradientColor.a);
    } else {
        finalColor = gradientColor;
    }
    
    gl_FragColor = finalColor * vColor;
}