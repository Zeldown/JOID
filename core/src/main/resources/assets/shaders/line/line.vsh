out vec4 vColor;

void main() {
    vColor = aColor;
    gl_Position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);
}