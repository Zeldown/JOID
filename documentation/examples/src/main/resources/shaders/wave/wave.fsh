in vec2 vTexCoord;
in vec4 vColor;

uniform float u_Time;

void main() {
	float wave = 0.5 + 0.5 * sin(vTexCoord.x * 24.0 + u_Time * 3.14159);
	fragColor = vec4(vColor.rgb * (0.6 + 0.4 * wave), vColor.a);
}