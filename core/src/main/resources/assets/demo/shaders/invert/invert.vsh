out vec2 vTexCoord;
out vec4 vColor;

void main() {
	vTexCoord = aTexCoord;
	vColor = aColor;

	gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);
}