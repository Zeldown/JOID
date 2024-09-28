#version 120

varying vec2 TexCoord;

uniform sampler2D tex;
uniform float u_Radius;
uniform vec4 u_InnerRect;
uniform int u_Type = 0;

varying vec2 f_Position;

void main() {
    vec2 tl = u_InnerRect.xy - f_Position;
    vec2 br = f_Position - u_InnerRect.zw;
    vec2 dis = max(br, tl);
    vec4 sample =  texture2D(tex, TexCoord);

    float v = length(max(vec2(0.0), dis)) - u_Radius;
    float a = 1.0 - smoothstep(0.0, 1.0, v);
    
    if (u_Type == 0) {
    	gl_FragColor = sample * vec4(1.0, 1.0, 1.0, a);
    } else {
    	gl_FragColor = gl_Color * vec4(1.0, 1.0, 1.0, a);
    }
}