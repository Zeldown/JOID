out vec4 vColor;
out float vAcross;
out float vAlong;
out float vLength;

uniform float u_Width;
uniform vec2 u_Viewport;

void main() {
    vec4 position = uProjectionMatrix * uModelViewMatrix * vec4(aPosition, 1.0);
    vec4 other = uProjectionMatrix * uModelViewMatrix * vec4(aTexCoord, aPosition.z, 1.0);

    vec2 halfViewport = u_Viewport * 0.5;
    vec2 screen = position.xy / position.w * halfViewport;
    vec2 otherScreen = other.xy / other.w * halfViewport;

    float segmentLength = max(distance(screen, otherScreen), 0.0001);
    float side = sign(aNormal.x);
    float end = sign(aNormal.y);

    vec2 direction = (otherScreen - screen) / segmentLength * -end;
    vec2 normal = vec2(-direction.y, direction.x);
    float halfWidth = u_Width * 0.5 + 1.0;

    vec2 offset = normal * side * halfWidth + direction * end;
    gl_Position = vec4(position.xy + offset / halfViewport * position.w, position.zw);

    vColor = aColor;
    vAcross = side * halfWidth;
    vAlong = end < 0.0 ? -1.0 : segmentLength + 1.0;
    vLength = segmentLength;
}