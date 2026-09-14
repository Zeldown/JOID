out vec2 vTexCoord;
out vec4 vColor;
out vec2 vPosition;

void main() {
    vTexCoord = aTexCoord;
    vPosition = aPosition.xy;
    vColor = aColor;

    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);
}