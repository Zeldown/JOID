#version 120

varying vec2 vPosition;
varying vec2 vTexCoord;
varying vec4 vColor;

uniform float u_Radius;
uniform vec4 u_InnerRect;

uniform sampler2D tex;

void main() {
    vec2 tl = u_InnerRect.xy - vPosition;
    vec2 br = vPosition - u_InnerRect.zw;
    vec2 distances = max(br, tl);
    
    float distanceToCorner = length(max(vec2(0.0), distances)) - u_Radius;
    
    vec4 baseColor;
    vec4 textureColor = texture2D(tex, vTexCoord);
    if (textureColor.a < 0.01 || (textureColor.r + textureColor.g + textureColor.b) < 0.01) {
        baseColor = vColor;
    } else {
        baseColor = textureColor;
    }
    
    float alpha = 1.0 - smoothstep(0.0, 1.0, distanceToCorner);
    gl_FragColor = baseColor * vec4(1.0, 1.0, 1.0, alpha);
}