#version 120

varying vec2 vPosition;
varying vec2 vTexCoord;
varying vec4 vColor;

uniform float u_Radius;
uniform vec4 u_InnerRect;
uniform int u_Type;

uniform sampler2D tex;

void main() {
    vec2 tl = u_InnerRect.xy - vPosition;
    vec2 br = vPosition - u_InnerRect.zw;
    vec2 distances = max(br, tl);
    
    float distanceToCorner = length(max(vec2(0.0), distances)) - u_Radius;
    
    vec4 baseColor = texture2D(tex, vTexCoord) * vColor;
    if (u_Type == 1) { // TEXTURE
    	baseColor = texture2D(tex, vTexCoord);
    } else if (u_Type == 2) { // COLOR
    	baseColor = vColor;
    }
    
    float mask = 1.0 - smoothstep(0.0, 1.0, distanceToCorner);
    float finalAlpha = baseColor.a * mask;

    gl_FragColor = vec4(baseColor.rgb, finalAlpha);
}