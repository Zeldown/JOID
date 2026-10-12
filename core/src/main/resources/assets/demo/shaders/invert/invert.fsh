in vec2 vTexCoord;
in vec4 vColor;

uniform sampler2D tex;
uniform float u_Amount;

void main() {
    vec4 color = texture(tex, vTexCoord) * vColor;
    fragColor = vec4(mix(color.rgb, color.aaa - color.rgb, u_Amount), color.a);
}