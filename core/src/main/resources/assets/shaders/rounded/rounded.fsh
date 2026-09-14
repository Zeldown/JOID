in vec2 vPosition;
in vec2 vTexCoord;
in vec4 vColor;

uniform float u_Radius;
uniform vec4 u_InnerRect;
uniform int u_Type;

uniform sampler2D tex;

vec2 snappedPosition() {
    return floor(vPosition * 256.0 + 0.5) / 256.0;
}

void main() {
    vec2 tl = u_InnerRect.xy - snappedPosition();
    vec2 br = snappedPosition() - u_InnerRect.zw;
    vec2 distances = max(br, tl);

    float distanceToCorner = length(max(vec2(0.0), distances)) - u_Radius;

    vec4 baseColor = texture(tex, vTexCoord) * vColor;
    if (u_Type == 1) {
        baseColor = texture(tex, vTexCoord);
    } else if (u_Type == 2) {
        baseColor = vColor;
    }

    float mask = 1.0 - smoothstep(0.0, 1.0, distanceToCorner);

    if (u_Type == 1) {
        fragColor = vec4(baseColor.rgb * mask, baseColor.a * mask);
    } else {
        fragColor = vec4(baseColor.rgb, baseColor.a * mask);
    }
}