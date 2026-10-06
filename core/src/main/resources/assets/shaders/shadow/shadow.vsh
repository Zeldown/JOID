out vec2 vPosition;
out vec4 vColor;

void main() {
    vPosition = aPosition.xy;
    vColor = aColor;

    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);
}