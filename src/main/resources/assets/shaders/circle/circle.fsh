#version 120

varying vec2 vTexCoord;
varying vec2 vPosition;
varying vec4 vColor;

uniform float radius;
uniform vec2 center;
uniform int type;

uniform sampler2D texture;

void main() {
    float dist = length(vPosition - center);
    
    vec4 baseColor = texture2D(texture, vTexCoord) * vColor;
    if (type == 1) { // TEXTURE
    	baseColor = texture2D(texture, vTexCoord);
    } else if (type == 2) { // COLOR
    	baseColor = vColor;
    }
    
    float mask = 1.0 - smoothstep(radius - 1.0, radius, dist);

    if (type == 1) {
        gl_FragColor = vec4(baseColor.rgb * mask, baseColor.a * mask);
    } else {
        gl_FragColor = vec4(baseColor.rgb, baseColor.a * mask);
    }
}