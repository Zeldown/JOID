#version 120

varying vec2 vTexCoord;
varying vec2 vPosition;
varying vec4 vColor;

uniform sampler2D tex;
uniform vec2 u_Direction;
uniform float u_Radius;
uniform vec2 u_TexelSize;

void main() {
    vec4 color = vec4(0.0);
    float totalWeight = 0.0;
    float sigma = max(u_Radius * 0.5, 1.0);
    float invSigma2 = 1.0 / (2.0 * sigma * sigma);
    float step = max(u_Radius / 32.0, 1.0);

    for (int i = -32; i <= 32; i++) {
        float offset = float(i) * step;
        float weight = exp(-(offset * offset) * invSigma2);
        vec4 s = texture2D(tex, vTexCoord + u_Direction * offset * u_TexelSize);
        color += vec4(s.rgb * s.a, s.a) * weight;
        totalWeight += weight;
    }

    color /= totalWeight;

    if (color.a > 0.001) {
        color.rgb /= color.a;
    }

    gl_FragColor = color;
}
