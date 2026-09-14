in vec2 vTexCoord;
in vec2 vPosition;
in vec4 vColor;

uniform float radius;
uniform vec2 center;
uniform int type;

uniform sampler2D tex;

void main() {
    float dist = length(vPosition - center);

    vec4 baseColor = texture(tex, vTexCoord) * vColor;
    if (type == 1) {
        baseColor = texture(tex, vTexCoord);
    } else if (type == 2) {
        baseColor = vColor;
    }

    float mask = 1.0 - smoothstep(radius - 1.0, radius, dist);

    if (type == 1) {
        fragColor = vec4(baseColor.rgb * mask, baseColor.a * mask);
    } else {
        fragColor = vec4(baseColor.rgb, baseColor.a * mask);
    }
}