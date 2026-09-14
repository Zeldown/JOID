in vec2 vTexCoord;
in vec4 vColor;
flat in vec4 vLitColor;

uniform sampler2D tex;

void main() {
    fragColor = texture(tex, vTexCoord) * (uLighting ? vLitColor : vColor);
}