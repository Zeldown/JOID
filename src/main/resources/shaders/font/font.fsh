#version 120

varying vec2 pos;

uniform sampler2D msdf;
uniform vec4 fgColor;

uniform float doffset;
uniform float hint_amount;

uniform vec2 sdf_texel;

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

float smoother(float edge0, float edge1, float x) {
    x = clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
    return x * x * (3.0 - 2.0 * x);
}

float getDistanceFromSDF(sampler2D msdf, vec2 pos) {
    vec3 raw = texture2D(msdf, pos).rgb;
    return median(raw.r, raw.g, raw.b);// - 0.5;
}

void main() {
    float sdf       = getDistanceFromSDF(msdf, pos);
    float sdf_north = getDistanceFromSDF(msdf, pos + vec2(0.0, sdf_texel.y));
    float sdf_east  = getDistanceFromSDF(msdf, pos + vec2(sdf_texel.x, 0.0));

    vec2  sgrad     = vec2(sdf_east - sdf, sdf_north - sdf);
    float sgrad_len = max(length(sgrad), 1.0 / 128.0);
    vec2  grad      = sgrad / vec2(sgrad_len);
    float vgrad     = abs(grad.y);

    float horz_scale  = 1.1;
    float vert_scale  = 0.8;
    float hdoffset    = mix(doffset * horz_scale, doffset * vert_scale, vgrad);
    float res_doffset = mix(doffset, hdoffset, hint_amount);

    float alpha       = smoother(0.5 - res_doffset, 0.5 + res_doffset, sdf);

    if (alpha < 20.0 / 256.0) {
    	discard;
    }

    gl_FragColor = vec4(fgColor.rgb, alpha * fgColor.a);
}